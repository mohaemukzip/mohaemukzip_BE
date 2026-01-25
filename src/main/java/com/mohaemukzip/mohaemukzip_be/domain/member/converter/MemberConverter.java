package com.mohaemukzip.mohaemukzip_be.domain.member.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;

public class MemberConverter {
    public static RecipeResponseDTO.RecipePreviewDTO toRecipePreviewDTO(
            Recipe recipe,
            Boolean isBookmarked
    ) {
        return RecipeResponseDTO.RecipePreviewDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .channelName(recipe.getChannel())
                .viewCount(recipe.getViews())
                .videoId(recipe.getVideoId())
                .channelId(recipe.getChannelId())
                .videoDuration(recipe.getTime())
                .cookingTimeMinutes(recipe.getCookingTime())
                .difficulty(recipe.getLevel())
                .isBookmarked(isBookmarked)
                .build();
    }
}