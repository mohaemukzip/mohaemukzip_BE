package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeDetailResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;

public interface RecipeQueryService {
    RecipeResponseDTO.RecipePreviewListDTO getRecipesByCategoryId(Long categoryId, Integer page, Member member);
    RecipeResponseDTO.RecipeDetailDTO getRecipeDetail(Long recipeId, Member member);
}
