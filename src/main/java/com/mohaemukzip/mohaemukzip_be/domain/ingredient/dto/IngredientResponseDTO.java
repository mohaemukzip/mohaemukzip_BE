package com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IngredientResponseDTO {
    private Long id;
    private String name;
    private String category;
    private String unit;

    //엔티티 -> dto 변환 메서드
    public static IngredientResponseDTO from(Ingredient ingredient) {

        return IngredientResponseDTO.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .category(ingredient.getCategory() != null ? ingredient.getCategory().getLabel() : null)
                .unit(ingredient.getUnit() != null ? ingredient.getUnit().getLabel() : null)
                .build();
    }
}
