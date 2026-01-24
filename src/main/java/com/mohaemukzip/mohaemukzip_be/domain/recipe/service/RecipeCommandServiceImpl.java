package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.RecipeIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.builder.GeminiPromptBuilder;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.CookingRecord;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.RecipeStep;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.GeminiResponseConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeIngredientConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeStepConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.CookingRecordRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeStepRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.SummaryRepository;
import com.mohaemukzip.mohaemukzip_be.global.service.PythonTranscriptExecutor;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecipeCommandServiceImpl implements RecipeCommandService {

    private static final int MAX_RECIPE_STEPS = 10;

    @Qualifier("geminiSummaryWebClient")
    private final WebClient geminiSummaryWebClient;
    private final RecipeRepository recipeRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final SummaryRepository summaryRepository;
    private final PythonTranscriptExecutor transcriptExecutor;


    private final RecipeConverter recipeConverter;
    private final RecipeIngredientConverter recipeIngredientConverter;
    private final GeminiResponseConverter geminiResponseConverter;
    private final RecipeStepConverter recipeStepConverter;
    private final GeminiPromptBuilder geminiPromptBuilder;
    private final RecipeCrawler recipeCrawler;

    public record SummaryCreateResult(boolean summaryExists, int stepCount) {}

    @Override
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
                .orElseThrow(() -> new IllegalArgumentException("레시피가 존재하지 않습니다."));

        // 3자막 추출 (Python)
        String transcriptJson =
                transcriptExecutor.fetchTranscriptJson(recipe.getVideoId());

        // Summary 생성
        Summary summary = createSummaryWithRaceConditionHandling(recipe, recipeId);

        // Gemini → step draft
        List<GeminiResponseConverter.StepDraft> steps =
                generateStepsFromGemini(recipe.getTitle(), transcriptJson);

        //  Step 저장
        List<RecipeStep> entities = recipeStepConverter.toEntities(summary, steps);

        recipeStepRepository.saveAll(entities);

        return new SummaryCreateResult(true, entities.size());
    }

    private Recipe saveRecipe(RecipeCrawler.RecipeData data) {
        Recipe recipe = recipeConverter.toEntity(data);

        try {
            return recipeRepository.save(recipe);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 저장된 레시피입니다. videoId=" + data.videoId(), e);
        }
    }

    private void saveRecipeIngredients(Recipe recipe, List<RecipeCrawler.IngredientData> ingredientDataList) {
        ingredientDataList.forEach(ingredientData ->
                ingredientRepository.findByName(ingredientData.name())
                        .ifPresentOrElse(
                                ingredient -> saveRecipeIngredient(recipe, ingredient, ingredientData),
                                () -> log.warn("재료 매칭 실패 - DB에 없음: {}", ingredientData.name())
                        )
        );
    }

    private void saveRecipeIngredient(
            Recipe recipe,
            Ingredient ingredient,
            RecipeCrawler.IngredientData ingredientData
    ) {
        RecipeIngredient recipeIngredient = recipeIngredientConverter.toEntity(recipe, ingredient, ingredientData);
        recipeIngredientRepository.save(recipeIngredient);
    }

    private Summary createSummaryWithRaceConditionHandling(Recipe recipe, Long recipeId) {
        try {
            return summaryRepository.save(
                    Summary.builder()
                            .recipe(recipe)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            return summaryRepository.findByRecipeId(recipeId)
                    .orElseThrow(() -> new RuntimeException("Race condition handling failed", e));
        }
    }


    private List<GeminiResponseConverter.StepDraft> generateStepsFromGemini(String recipeTitle, String transcriptJson) {
        String prompt = geminiPromptBuilder.buildRecipeStepPrompt(recipeTitle, transcriptJson);
        String responseBody = callGeminiApi(prompt);
        return geminiResponseConverter.convertToStepDrafts(responseBody, MAX_RECIPE_STEPS);
    }

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

        return geminiSummaryWebClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
