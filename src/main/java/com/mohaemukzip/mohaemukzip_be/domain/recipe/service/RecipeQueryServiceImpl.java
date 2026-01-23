package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.RecipeIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeDetailResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeCategoryRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeStepRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.SummaryRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeQueryServiceImpl implements RecipeQueryService {

    private final RecipeCategoryRepository recipeCategoryRepository;
    private static final int PAGE_SIZE = 10;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final SummaryRepository summaryRepository;
    private final MemberIngredientRepository memberIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;

    @Override
    public RecipeResponseDTO.RecipePreviewListDTO getRecipesByCategoryId(Long categoryId, Integer page) {
        Page<Recipe> recipePage = recipeCategoryRepository.findRecipesByCategoryId(categoryId, PageRequest.of(page, PAGE_SIZE));

        // 첫 페이지인데 데이터가 없다면 -> 존재하지 않는 카테고리로 간주
        if (page == 0 && recipePage.isEmpty()) {
            throw new BusinessException(ErrorStatus.CATEGORY_NOT_FOUND);
        }

        return RecipeConverter.toRecipePreviewListDTO(recipePage);
    }

    @Override
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId, Long memberId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("레시피가 존재하지 않습니다."));

        List<RecipeIngredient> recipeIngredients =
                recipeIngredientRepository.findAllByRecipeId(recipeId);

        // recipe에 포함된 ingredientId 목록 추출
        List<Long> ingredientIds = recipeIngredients.stream()
                .map(ri -> ri.getIngredient().getId())
                .toList();

        // 유저가 보유한 재료 id Set 조회 (N+1 방지)
        Set<Long> memberIngredientIds =
                memberIngredientRepository.findIngredientIdsByMemberIdAndIngredientIdIn(
                        memberId,
                        ingredientIds
                );

        boolean summaryExists = summaryRepository.existsByRecipeId(recipeId);

        // summary 조회
        Summary summary = summaryRepository.findByRecipeId(recipeId).orElse(null);

        List<RecipeDetailResponseDTO.RecipeStepResponse> steps = List.of();


        if (summary != null) {
            steps = recipeStepRepository
                    .findAllBySummaryIdOrderByStepNumberAsc(summary.getId())
                    .stream()
                    .map(step -> RecipeDetailResponseDTO.RecipeStepResponse.builder()
                            .stepNumber(step.getStepNumber())
                            .title(step.getTitle())
                            .description(step.getDescription())
                            .videoTime(RecipeDetailResponseDTO.RecipeStepResponse
                                    .formatVideoTime(step.getVideoTime()))
                            .build()
                    )
                    .toList();
        }

        if (steps.size() > 10) {
            steps = steps.subList(0, 10);
        }

        return RecipeDetailResponseDTO.builder()
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .imageUrl(recipe.getImageUrl())
                .videoUrl(recipe.getVideoUrl())
                .videoId(recipe.getVideoId())
                .channel(recipe.getChannel())
                .cookingTime(recipe.getCookingTime())
                .views(recipe.getViews())
                .level(recipe.getLevel())
                .ratingCount(recipe.getRatingCount())
                .ingredients(
                        recipeIngredients.stream()
                                .map(ri -> RecipeDetailResponseDTO.IngredientResponse.builder()
                                        .ingredientId(ri.getIngredient().getId())
                                        .name(ri.getIngredient().getName())
                                        .amount(ri.getAmount())
                                        .unit(
                                                ri.getIngredient().getUnit() != null
                                                        ? ri.getIngredient().getUnit().name()
                                                        : null
                                        )
                                        .hasIngredient(
                                                memberIngredientIds.contains(
                                                        ri.getIngredient().getId()
                                                )
                                        )
                                        .build()
                                )
                                .toList()
                )
                .summaryExists(summaryExists)
                .steps(steps)
                .build();
    }

}
