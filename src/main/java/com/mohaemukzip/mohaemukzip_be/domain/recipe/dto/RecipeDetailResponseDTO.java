package com.mohaemukzip.mohaemukzip_be.domain.recipe.dto;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDetailResponseDTO {

    private Long recipeId;
    private String title;
    private String imageUrl;
    private String videoUrl;
    private String channel;
    private Integer cookingTime;
    private Integer views;
    private String videoId;

    private Double level;
    private Integer ratingCount;

    private List<IngredientResponse> ingredients;
    private boolean summaryExists;
    private List<RecipeStepResponse> steps;

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IngredientResponse {
        private Long ingredientId;
        private String name;
        private Double amount; // ex) 200.0
        private String unit;      // ex) "g"
        private Boolean hasIngredient; // 유저가 가지고 있는 재료면 true

        public static IngredientResponse from(RecipeIngredient ri) {
            return IngredientResponse.builder()
                    .ingredientId(ri.getIngredient().getId())
                    .name(ri.getIngredient().getName())
                    .amount(ri.getAmount())
                    .unit(
                            ri.getIngredient().getUnit() != null
                                    ? ri.getIngredient().getUnit().name()
                                    : null
                    )
                    .build();
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeStepResponse {
        private Integer stepNumber;
        private String title;
        private String description;
        private String videoTime; // "12:35"

        public static String formatVideoTime(Integer seconds) {
            if (seconds == null) return null;

            int min = seconds / 60;
            int sec = seconds % 60;

            return String.format("%d:%02d", min, sec);
        }
    }


}
