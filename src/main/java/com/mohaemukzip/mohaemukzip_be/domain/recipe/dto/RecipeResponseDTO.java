package com.mohaemukzip.mohaemukzip_be.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class RecipeResponseDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipePreviewDTO {
        private Long recipeId;
        private String title;
        private String imageUrl;
        private Integer cookingTime;
        private Double level;
        private Integer ratingCount;
    }

    @Builder
    @Getter
    @NoArgsConstructor
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


    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CookingRecordCreateResponseDTO {

        private Long cookingRecordId;
        private Long recipeId;
        private Integer rating;      // 사용자가 준 별점 (1~5)
        private Double recipeLevel;  // 갱신된 레시피 난이도
        private Integer ratingCount; // 갱신된 평가 수
    }
}
