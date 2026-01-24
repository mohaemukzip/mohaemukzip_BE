package com.mohaemukzip.mohaemukzip_be.domain.recipe.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

public class RecipeResponseDTO {

    @SuperBuilder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class RecipeCommonDTO {
        private Long id;
        private String title;
        private String channelName;
        private Long viewCount;
        private String videoId;
        private String channelId;
        private String videoDuration;
        private Integer cookingTimeMinutes;
        private Double difficulty;
        private Boolean isBookmarked;
    }

    @SuperBuilder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecipePreviewDTO extends RecipeCommonDTO {
        // 필드가 없으므로 @AllArgsConstructor 제거 (NoArgsConstructor와 충돌 방지)
    }

    @Builder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class RecipePreviewListDTO {
        private List<RecipePreviewDTO> recipeList;
        private Integer listSize;
        private Integer totalPage;
        private Long totalElements;
        private Boolean isFirst;
        private Boolean isLast;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeCreateRequest {
        private String videoId;
    }

    public static record RecipeCreateResponse(Long recipeId) {}


    @SuperBuilder
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class RecipeDetailDTO extends RecipeCommonDTO {
        private List<String> ingredients;
        private List<String> instructions;
    }
}
