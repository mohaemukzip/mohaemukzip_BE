package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.RecipeCrawler;
import com.mohaemukzip.mohaemukzip_be.global.util.TimeFormatter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
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

    public Recipe toEntity(RecipeCrawler.RecipeData data) {
        Category category = convertCategory(data.category());

        return Recipe.builder()
                .title(data.title())
                .level(0.0)
                .ratingCount(0)
                .time(data.time())
                .cookingTime(data.cookingTime())
                .channel(data.channelTitle())
                .channelId(data.channelId())
                .views(data.viewCount())
                .imageUrl(data.thumbnailUrl())
                .category(category)
                .channelProfileImageUrl(data.channelProfileImageUrl())
                .videoId(data.videoId())
                .videoUrl(data.videoUrl())
                .build();
    }

    private Category convertCategory(String categoryName) {
        return Arrays.stream(Category.values())
                .filter(c -> c.name().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 카테고리: " + categoryName));
    }
}
