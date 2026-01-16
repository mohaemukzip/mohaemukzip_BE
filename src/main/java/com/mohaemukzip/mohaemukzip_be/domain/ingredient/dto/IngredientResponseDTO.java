package com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import lombok.Builder;
import lombok.Getter;

public class IngredientResponseDTO {

    //1. 재료 조회 api
    @Getter
    @Builder
    public static class Detail {
        private Long id;
        private String name;
        private String category;
        private String unit;

        // 엔티티 -> DTO 변환 메서드
        public static Detail from(Ingredient ingredient) {
            return Detail.builder()
                    .id(ingredient.getId())
                    .name(ingredient.getName())
                    .category(ingredient.getCategory() != null ? ingredient.getCategory().getLabel() : null)
                    .unit(ingredient.getUnit() != null ? ingredient.getUnit().getLabel() : null)
                    .build();
        }
    }

    //2. 냉장고에 추가 api
    @Getter
    @Builder
    public static class AddFridgeResult {
        private Long memberIngredientId;
    }
}
