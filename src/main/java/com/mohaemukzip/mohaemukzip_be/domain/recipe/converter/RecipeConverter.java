package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.global.util.TimeFormatter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeConverter {

    public static RecipeResponseDTO.RecipePreviewDTO toRecipePreviewDTO(Recipe recipe, boolean isBookmarked) {
        return RecipeResponseDTO.RecipePreviewDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .channelName(recipe.getChannel())
                .viewCount(recipe.getViews())
                .videoId(recipe.getVideoId())
                .channelId(recipe.getChannelId())
                .videoDuration(TimeFormatter.formatDuration(recipe.getTime()))
                .cookingTimeMinutes(recipe.getCookingTime())
                .difficulty(recipe.getLevel()) // Recipe.level은 Integer이므로 자동 매핑됨. 만약 DB가 Double이라면 (int) Math.round() 필요하나 현재 Entity는 Integer임.
                .isBookmarked(isBookmarked)
                .build();
    }

    public static RecipeResponseDTO.RecipePreviewListDTO toRecipePreviewListDTO(Page<Recipe> recipePage, Set<Long> bookmarkedRecipeIds) {
        List<RecipeResponseDTO.RecipePreviewDTO> recipePreviewDTOList = recipePage.stream()
                .map(recipe -> toRecipePreviewDTO(recipe, bookmarkedRecipeIds.contains(recipe.getId())))
                .collect(Collectors.toList());

        return RecipeResponseDTO.RecipePreviewListDTO.builder()
                .isLast(recipePage.isLast())
                .isFirst(recipePage.isFirst())
                .totalPage(recipePage.getTotalPages())
                .totalElements(recipePage.getTotalElements())
                .listSize(recipePreviewDTOList.size())
                .recipeList(recipePreviewDTOList)
                .build();
    }

    public static RecipeResponseDTO.RecipeDetailDTO toRecipeDetailDTO(Recipe recipe, boolean isBookmarked, List<String> ingredients, List<String> instructions) {
        return RecipeResponseDTO.RecipeDetailDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .channelName(recipe.getChannel())
                .viewCount(recipe.getViews())
                .videoId(recipe.getVideoId())
                .channelId(recipe.getChannelId())
                .videoDuration(TimeFormatter.formatDuration(recipe.getTime()))
                .cookingTimeMinutes(recipe.getCookingTime())
                .difficulty(recipe.getLevel())
                .isBookmarked(isBookmarked)
                .ingredients(ingredients)
                .instructions(instructions)
                .build();
    }
}
