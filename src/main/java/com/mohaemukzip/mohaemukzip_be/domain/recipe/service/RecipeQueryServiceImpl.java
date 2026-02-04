package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.RecipeIngredientRepository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeDetailConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeDetailResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Category;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.RecipeStep;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.*;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.service.RecentlyViewedRecipeManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeQueryServiceImpl implements RecipeQueryService {

    private final MemberRecipeRepository memberRecipeRepository;
    private static final int PAGE_SIZE = 10;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final SummaryRepository summaryRepository;
    private final MemberIngredientRepository memberIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeDetailConverter recipeDetailConverter;

    private final RecentlyViewedRecipeManager recentlyViewedRecipeManager;


    @Override
    public RecipeResponseDTO.RecipePreviewListDTO getRecipesByCategoryId(Long categoryId, Integer page, Long memberId) {
        Page<Recipe> recipePage = recipeRepository.findRecipesByDishCategoryId(categoryId, PageRequest.of(page, PAGE_SIZE));

        // 첫 페이지인데 데이터가 없다면 -> 존재하지 않는 카테고리로 간주
        if (page == 0 && recipePage.isEmpty()) {
            throw new BusinessException(ErrorStatus.CATEGORY_NOT_FOUND);
        }

        Set<Long> bookmarkedRecipeIds = Collections.emptySet();
        if (memberId != null && !recipePage.isEmpty()) {
            List<Long> recipeIds = recipePage.getContent().stream()
                    .map(Recipe::getId)
                    .collect(Collectors.toList());
            bookmarkedRecipeIds = memberRecipeRepository
                    .findBookmarkedRecipeIdsByMemberId(memberId, recipeIds);
        }

        return RecipeConverter.toRecipePreviewListDTO(recipePage, bookmarkedRecipeIds);
    }

    @Override
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId, Long memberId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.RECIPE_NOT_FOUND));

        boolean isBookmarked =
                memberRecipeRepository.existsByMember_IdAndRecipe_Id(memberId, recipeId);

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

        List<RecipeStep> steps = fetchRecipeSteps(recipeId);

        recentlyViewedRecipeManager.add(memberId, recipeId);

        return recipeDetailConverter.toDTO(
                recipe,
                recipeIngredients,
                memberIngredientIds,
                summaryExists,
                steps,
                isBookmarked
        );
    }

    private List<RecipeStep> fetchRecipeSteps(Long recipeId) {
        Summary summary = summaryRepository.findByRecipeId(recipeId).orElse(null);

        if (summary == null) {
            return List.of();
        }

        return recipeStepRepository.findAllBySummaryIdOrderByStepNumberAsc(summary.getId());
    }
}
