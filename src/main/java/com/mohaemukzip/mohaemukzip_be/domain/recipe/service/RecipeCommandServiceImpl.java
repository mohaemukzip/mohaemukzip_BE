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
    @Transactional
    public Long saveRecipeByVideoId(String videoId) {

        // 중복 방지
        if (recipeRepository.existsByVideoId(videoId)) {
            throw new BusinessException(ErrorStatus.RECIPE_ALREADY_EXISTS);
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
                .orElseThrow(() -> new BusinessException(ErrorStatus.RECIPE_NOT_FOUND));

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
    public RecipeResponseDTO.BookmarkToggleResult toggleBookmark(Long memberId, Long recipeId) {
        // 1. 삭제 시도 (Bulk Delete)
        int deletedCount = memberRecipeRepository.deleteByMemberIdAndRecipeId(memberId, recipeId);

        if (deletedCount > 0) {
            // 삭제 성공 -> 북마크 해제됨
            return RecipeConverter.toBookmarkToggleResult(false);
        } else {
            // 2. 삭제된 게 없으면 -> 저장 시도
            // Proxy 객체 조회 (DB Select 방지)
            Recipe recipeRef = recipeRepository.getReferenceById(recipeId);
            Member memberRef = memberRepository.getReferenceById(memberId);

            try {
                memberRecipeRepository.save(
                        MemberRecipe.builder()
                                .member(memberRef)
                                .recipe(recipeRef)
                                .build()
                );
                return RecipeConverter.toBookmarkToggleResult(true);
            } catch (DataIntegrityViolationException e) {
                // 동시성 이슈 또는 FK 위반 가능 -> 실제 저장 여부 확인
                boolean actuallyExists = memberRecipeRepository.existsByMember_IdAndRecipe_Id(memberId, recipeId);
                if (actuallyExists) {
                    log.warn("Bookmark race condition detected for memberId={}, recipeId={}", memberId, recipeId);
                    return RecipeConverter.toBookmarkToggleResult(true);
                } else {
                    // 레시피가 존재하지 않음 (FK 위반)
                    throw new BusinessException(ErrorStatus.RECIPE_NOT_FOUND);
                }
            }
        }
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