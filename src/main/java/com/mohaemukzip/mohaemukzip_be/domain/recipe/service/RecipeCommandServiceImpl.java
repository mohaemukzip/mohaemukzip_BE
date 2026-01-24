package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.RecipeIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.CookingRecord;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.RecipeStep;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.CookingRecordRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeStepRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.SummaryRepository;
import com.mohaemukzip.mohaemukzip_be.util.PythonTranscriptExecutor;
import com.mohaemukzip.mohaemukzip_be.util.RecipeCrawler;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecipeCommandServiceImpl implements RecipeCommandService {

    private final RecipeRepository recipeRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeCrawler recipeCrawler;

    private final PythonTranscriptExecutor transcriptExecutor;
    private final RecipeStepRepository recipeStepRepository;
    private final SummaryRepository summaryRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;



    public void rateRecipe(Long memberId, Long recipeId, int rating) {
        Recipe recipe = recipeRepository.findByIdForUpdate(recipeId);
        if (recipe == null) {
            throw new IllegalArgumentException("레시피가 존재하지 않습니다.");
        }
        recipe.addRating(rating);

        CookingRecord record = CookingRecord.builder()
                .member(Member.builder().id(memberId).build())
                .recipe(recipe)
                .rating(rating)
                .build();

        cookingRecordRepository.save(record);
    }

    /**
     * videoId로 레시피 저장 (Recipe + RecipeIngredient)
     */
    @Transactional
    public Long saveRecipeByVideoId(String videoId) {

        // 중복 방지
        if (recipeRepository.existsByVideoId(videoId)) {
            throw new IllegalStateException("이미 저장된 레시피입니다. videoId=" + videoId);
        }

        // Gemini 프롬프트용 재료 이름 조회
        List<String> ingredientNames = ingredientRepository.findAllNames();

        // 크롤링
        RecipeCrawler.RecipeData data =
                recipeCrawler.crawlRecipe(videoId, ingredientNames);

        // Recipe 저장
        Category category = Arrays.stream(Category.values())
                .filter(c -> c.name().equalsIgnoreCase(data.category()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 카테고리: " + data.category()));
        Recipe recipe = Recipe.builder()
                .title(data.title())
                .level(0.0)
                .ratingCount(0)
                .time(data.time())
                .cookingTime(data.cookingTime())
                .channel(data.channelTitle())
                .channelId(data.channelId())
                .views(data.viewCount())
                .imageUrl(data.thumbnailUrl())
                .category(category)
                .channelProfileImageUrl(data.channelProfileImageUrl())
                .videoId(data.videoId())
                .videoUrl(data.videoUrl())
                .build();

        try {
                recipeRepository.save(recipe);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalStateException("이미 저장된 레시피입니다. videoId=" + videoId, e);
            }

        // RecipeIngredient 저장
        for (RecipeCrawler.IngredientData ingredientData : data.ingredients()) {

            ingredientRepository.findByName(ingredientData.name())
                    .ifPresentOrElse(ingredient -> {

                        Double amount = null;
                        try {
                            amount = Double.valueOf(ingredientData.amount());
                        } catch (NumberFormatException e) {
                            log.warn("amount 파싱 실패: {}", ingredientData.amount());
                        }
                        RecipeIngredient recipeIngredient = RecipeIngredient.builder()
                                .recipe(recipe)
                                .ingredient(ingredient)
                                .amount(amount)
                                .build();

                        recipeIngredientRepository.save(recipeIngredient);

                    }, () -> {
                        log.warn("❌ 재료 매칭 실패 - DB에 없음: {}", ingredientData.name());
                    });
        }

        log.info("레시피 저장 완료 - recipeId={}, videoId={}",
                recipe.getId(), videoId);

        return recipe.getId();
    }

    @Transactional
    @Override
    public SummaryCreateResult createSummary(Long recipeId) {

        //  이미 요약 존재 → 멱등
        Summary existing = summaryRepository.findByRecipeId(recipeId).orElse(null);
        if (existing != null) {
            int count = recipeStepRepository
                    .findAllBySummaryIdOrderByStepNumberAsc(existing.getId())
                    .size();
            return new SummaryCreateResult(true, count);
        }

        // Recipe 조회
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피가 존재하지 않습니다."));

        // 3자막 추출 (Python)
        String transcriptJson =
                transcriptExecutor.fetchTranscriptJson(recipe.getVideoId());

        // Summary 생성
        Summary summary;
        try {
            summary = summaryRepository.save(
                    Summary.builder()
                            .recipe(recipe)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            // 동시에 다른 요청이 summary 만든 경우
            Summary raced = summaryRepository.findByRecipeId(recipeId)
                    .orElseThrow(() -> e);
            int count = recipeStepRepository
                    .findAllBySummaryIdOrderByStepNumberAsc(raced.getId())
                    .size();
            return new SummaryCreateResult(true, count);
        }

        // Gemini → step draft
        List<StepDraft> steps =
                generateStepsFromGemini(recipe.getTitle(), transcriptJson);

        //  Step 저장
        List<RecipeStep> entities = steps.stream()
                .map(s -> RecipeStep.builder()
                        .summary(summary)
                        .stepNumber(s.stepNumber())
                        .title(s.title())
                        .description(s.description())
                        .videoTime(s.videoTime())
                        .build()
                )
                .toList();

        recipeStepRepository.saveAll(entities);

        return new SummaryCreateResult(true, entities.size());
    }

    private List<StepDraft> generateStepsFromGemini(String recipeTitle, String transcriptJson) {

        String prompt = buildStepPrompt(recipeTitle, transcriptJson);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = String.format(
                "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=%s",
                geminiApiKey
        );

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Gemini 호출 실패: " + response.getStatusCode());
         }

        JsonNode root;
        try {
            root = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }

        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new RuntimeException("Gemini candidates 결과가 비어있습니다.");
        }
        JsonNode textNode = candidates.get(0)
                .path("content").path("parts").path(0).path("text");
        if (textNode.isMissingNode()) {
            throw new RuntimeException("Gemini text 결과가 비어있습니다.");
        }
        String rawText = textNode.asText();

        String cleanedJson = stripCodeBlock(rawText);

        JsonNode resultNode;
        try {
            resultNode = objectMapper.readTree(cleanedJson);
        } catch (Exception e) {
            log.error("Gemini cleaned json:\n{}", cleanedJson);
            throw new RuntimeException("Gemini JSON 파싱 실패", e);
        }

        JsonNode stepsNode = resultNode.path("steps");
        if (!stepsNode.isArray() || stepsNode.isEmpty()) {
            throw new RuntimeException("Gemini steps 결과가 비어있습니다.");
        }

        List<StepDraft> steps = new ArrayList<>();
        for (JsonNode node : stepsNode) {
            steps.add(new StepDraft(
                    node.path("stepNumber").asInt(),
                    node.path("title").asText(),
                    node.path("description").asText(),
                    node.path("videoTime").isMissingNode() || node.path("videoTime").isNull()
                            ? null
                            : node.path("videoTime").asInt()
            ));
        }

        if (steps.size() > 10) {
            steps = steps.subList(0, 10);
        }
        return steps;
    }

    private String stripCodeBlock(String text) {
        if (text == null) return null;
        text = text.trim();

        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*", "");
            text = text.replaceFirst("```$", "");
        }
        return text.trim();
    }

    private String buildStepPrompt(String recipeTitle, String transcriptJson) {
        return String.format("""
    너는 요리 레시피 요약 어시스턴트다.
    
    레시피 제목: %s
    
    다음은 유튜브 영상 자막이다. JSON 배열이며, 각 원소는 text/start/duration을 가진다:
    %s
    
    자막을 기반으로 '요약 레시피 STEP 목록'을 만들어라.
    
    규칙:
    - steps는 시간 순으로 정렬
    - stepNumber는 1부터 연속 정수
    - 각 step은 title(짧고 행동 중심), description(자막 기반 요약) 포함
    - videoTime은 해당 step이 시작되는 대표 시점을 '초' 단위 정수로 넣어라.
      start 값을 참고해서 정수로 반올림/내림해서 넣어라. 정말 판단 불가능하면 null.
    - JSON만 출력(마크다운/코드블럭/설명 금지)
    - step은 최대 10개까지만 생성하라. 자막이 길더라도 가장 중요한 단계 10개만 선택하라.
    
    출력 스키마:
    {
      "steps": [
        {"stepNumber": 1, "title": "고기와 기본 재료 준비하기", "description": "…", "videoTime": 304}
      ]
    }
    """, recipeTitle, transcriptJson);
    }

    public record SummaryCreateResult(boolean summaryExists, int stepCount) {}
    private record StepDraft(int stepNumber, String title, String description, Integer videoTime) {}


}
