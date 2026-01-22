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
        private Integer level;
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
}
