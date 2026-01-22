package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
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
                .videoDuration(formatDuration(recipe.getTime()))
                .cookingTimeMinutes(recipe.getCookingTime())
                .difficulty(recipe.getLevel())
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

    private static String formatDuration(String time) {
        if (time == null || time.isEmpty()) {
            return "00:00";
        }
        // DB에 이미 "MM:SS" 또는 "HH:MM:SS" 형태로 저장되어 있다고 가정하거나,
        // 초 단위 정수라면 변환 로직 필요.
        // 현재 Recipe 엔티티 주석에 "10:54" (영상 길이) 라고 되어 있으므로 그대로 반환하거나 포맷팅 검증.
        // 만약 초 단위 정수가 문자열로 저장된 경우라면 변환 로직 추가 필요.
        // 여기서는 이미 포맷팅된 문자열이라고 가정하고 반환하되, 필요시 파싱 로직 추가 가능.
        return time;
    }
}
