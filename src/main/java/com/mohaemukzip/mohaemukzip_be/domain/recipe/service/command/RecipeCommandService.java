package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;

public interface RecipeCommandService {

    Long saveRecipeByVideoId(String videoId);

    RecipeResponseDTO.SummaryCreateResult createSummary(Long recipeId);

    RecipeResponseDTO.CookingRecordCreateResponseDTO createCookingRecord(
            Long memberId,
            Long recipeId,
            int rating
    );

    RecipeResponseDTO.BookmarkToggleResult toggleBookmark(Long memberId, Long recipeId);
}