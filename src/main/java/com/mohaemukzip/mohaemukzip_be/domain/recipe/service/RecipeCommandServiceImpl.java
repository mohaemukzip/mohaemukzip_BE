package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.RecipeIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.builder.GeminiPromptBuilder;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.*;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.GeminiResponseConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeIngredientConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeStepConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.*;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.service.PythonTranscriptExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeCommandServiceImpl implements RecipeCommandService {

    private static final int MAX_RECIPE_STEPS = 10;
    private static final Duration GEMINI_API_TIMEOUT = Duration.ofSeconds(30);

    @Qualifier("geminiSummaryWebClient")
    private final WebClient geminiSummaryWebClient;
    private final RecipeRepository recipeRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final SummaryRepository summaryRepository;
    private final MemberRecipeRepository memberRecipeRepository;
    private final MemberRepository memberRepository;
    private final PythonTranscriptExecutor transcriptExecutor;

    private final RecipeConverter recipeConverter;
    private final RecipeIngredientConverter recipeIngredientConverter;
    private final GeminiResponseConverter geminiResponseConverter;
    private final RecipeStepConverter recipeStepConverter;
    private final GeminiPromptBuilder geminiPromptBuilder;
    private final RecipeCrawler recipeCrawler;

    public record SummaryCreateResult(boolean summaryExists, int stepCount) {}

    /**
     * videoId로 레시피 저장 (Recipe + RecipeIngredient)
     */
    @Override
    @Transactional
    public Long saveRecipeByVideoId(String videoId) {
        // 중복 방지
        if (recipeRepository.existsByVideoId(videoId)) {
            throw new BusinessException(ErrorStatus.RECIPE_ALREADY_EXISTS);
        }

        // Gemini 프롬프트용 재료 이름 조회
        List<String> ingredientNames = ingredientRepository.findAllNames();

        // 크롤링
        RecipeCrawler.RecipeData data = recipeCrawler.crawlRecipe(videoId, ingredientNames);

        Recipe recipe = saveRecipe(data);
        saveRecipeIngredients(recipe, data.ingredients());
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
                .orElseThrow(() -> new BusinessException(ErrorStatus.RECIPE_NOT_FOUND));

        // 3자막 추출 (Python)
        String transcriptJson =
                transcriptExecutor.fetchTranscriptJson(recipe.getVideoId());

        // Summary 생성
        Summary summary = createSummaryWithRaceConditionHandling(recipe);

        // Gemini → step draft
        List<GeminiResponseConverter.StepDraft> steps =
                generateStepsFromGemini(recipe.getTitle(), transcriptJson);

        //  Step 저장
        List<RecipeStep> entities = recipeStepConverter.toEntities(summary, steps);

        recipeStepRepository.saveAll(entities);

        return new SummaryCreateResult(true, entities.size());
    }

    @Override
    @Transactional
    public RecipeResponseDTO.CookingRecordCreateResponseDTO createCookingRecord(
            Long memberId,
            Long recipeId,
            int rating
    ) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ErrorStatus.INVALID_RATING);
        }

        Recipe recipe = recipeRepository.findByIdForUpdate(recipeId);
        if (recipe == null) {
            throw new BusinessException(ErrorStatus.RECIPE_NOT_FOUND);
        }

        recipe.addRating(rating); // level, ratingCount 내부에서 갱신

        CookingRecord record = cookingRecordRepository.save(
                CookingRecord.builder()
                        .member(Member.builder().id(memberId).build())
                        .recipe(recipe)
                        .rating(rating)
                        .build()
        );

        return RecipeResponseDTO.CookingRecordCreateResponseDTO.builder()
                .cookingRecordId(record.getId())
                .recipeId(recipe.getId())
                .rating(rating)
                .recipeLevel(recipe.getLevel())
                .ratingCount(recipe.getRatingCount())
                .build();
    }

    @Override
    @Transactional
    public RecipeResponseDTO.BookmarkToggleResult toggleBookmark(Long memberId, Long recipeId) {
        // 1. 삭제 시도 (Bulk Delete)
        int deletedCount = memberRecipeRepository.deleteByMemberIdAndRecipeId(memberId, recipeId);

        if (deletedCount > 0) {
            return RecipeConverter.toBookmarkToggleResult(false);
        }

        // 2. 삭제 실패 = 북마크 추가 필요
        // 레시피 존재 확인 및 조회 (한 번만)
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.RECIPE_NOT_FOUND));

        Member member = memberRepository.getReferenceById(memberId);

        memberRecipeRepository.save(
                MemberRecipe.builder()
                        .member(member)
                        .recipe(recipe)
                        .build()
        );

        return RecipeConverter.toBookmarkToggleResult(true);
    }

    private Recipe saveRecipe(RecipeCrawler.RecipeData data) {
        Recipe recipe = recipeConverter.toEntity(data);

        try {
            return recipeRepository.save(recipe);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorStatus.RECIPE_ALREADY_EXISTS);
        }
    }

    private void saveRecipeIngredients(Recipe recipe, List<RecipeCrawler.IngredientData> ingredientDataList) {
        //모든 재료명 추출
        List<String> ingredientNames = ingredientDataList.stream()
                .map(RecipeCrawler.IngredientData::name)
                .toList();

        //한 번에 조회
        List<Ingredient> foundIngredients = ingredientRepository.findAllByNameIn(ingredientNames);

        Map<String, Ingredient> ingredientMap = foundIngredients.stream()
                .collect(Collectors.toMap(Ingredient::getName, Function.identity()));


        List<RecipeIngredient> recipeIngredients = ingredientDataList.stream()
                .map(data -> {
                    Ingredient ingredient = ingredientMap.get(data.name());
                    if (ingredient == null) {
                        log.warn("재료 매칭 실패 - DB에 없음: {}", data.name());
                        return null;
                    }
                    return recipeIngredientConverter.toEntity(recipe, ingredient, data);
                })
                .filter(Objects::nonNull)
                .toList();

        if (!recipeIngredients.isEmpty()) {
            recipeIngredientRepository.saveAll(recipeIngredients);
        }
    }

    private Summary createSummaryWithRaceConditionHandling(Recipe recipe) {
        try {
            return summaryRepository.save(
                    Summary.builder()
                            .recipe(recipe)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            // 동시성으로 인해 이미 생성된 경우
            log.info("Summary 동시 생성 감지 - recipeId: {}", recipe.getId());
            return summaryRepository.findByRecipeId(recipe.getId())
                    .orElseThrow(() -> new BusinessException(ErrorStatus.SUMMARY_CREATION_FAILED));
        }
    }

    /**
     * Gemini API를 통해 레시피 단계 생성
     */
    private List<GeminiResponseConverter.StepDraft> generateStepsFromGemini(String recipeTitle, String transcriptJson) {
        String prompt = geminiPromptBuilder.buildRecipeStepPrompt(recipeTitle, transcriptJson);
        String responseBody = callGeminiApi(prompt);
        return geminiResponseConverter.convertToStepDrafts(responseBody, MAX_RECIPE_STEPS);
    }

    /**
     * Gemini API 호출 (에러 처리 포함)
     */
    private String callGeminiApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        try {
            return geminiSummaryWebClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError(),
                            response -> {
                                log.error("Gemini API 클라이언트 에러 - Status: {}", response.statusCode());
                                return Mono.error(new BusinessException(ErrorStatus.GEMINI_BAD_REQUEST));
                            }
                    )
                    .onStatus(
                            status -> status.is5xxServerError(),
                            response -> {
                                log.error("Gemini API 서버 에러 - Status: {}", response.statusCode());
                                return Mono.error(new BusinessException(ErrorStatus.GEMINI_SERVER_ERROR));
                            }
                    )
                    .bodyToMono(String.class)
                    .timeout(GEMINI_API_TIMEOUT)
                    .block();

        } catch (WebClientException e) {
            log.error("Gemini API 호출 실패 - WebClient 예외", e);
            throw new BusinessException(ErrorStatus.EXTERNAL_API_ERROR);
        } catch (Exception e) {
            log.error("Gemini API 예상치 못한 오류", e);
            throw new BusinessException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}