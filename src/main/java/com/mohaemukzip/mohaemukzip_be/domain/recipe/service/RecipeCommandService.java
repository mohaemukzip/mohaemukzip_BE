package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;

public interface RecipeCommandService {

    void rateRecipe(Long memberId, Long recipeId, int rating);

    Long saveRecipeByVideoId(String videoId);

    RecipeCommandServiceImpl.SummaryCreateResult createSummary(Long recipeId);

    RecipeResponseDTO.CookingRecordCreateResponseDTO createCookingRecord(
            Long memberId,
            Long recipeId,
            int rating
    );
}
