package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeDetailResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;

public interface RecipeQueryService {
    RecipeResponseDTO.RecipePreviewListDTO getRecipesByCategoryId(Long categoryId, Integer page, Long memberId);
    RecipeDetailResponseDTO getRecipeDetail(Long recipeId, Long memberId);
    RecipeResponseDTO.RecipePreviewListDTO getRecipesByDishId(Long dishId, Integer page, Long memberId);
}