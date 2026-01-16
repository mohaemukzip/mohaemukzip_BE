package com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

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

    //3-1. 개별 냉장고 재료 정보
    @Getter
    @Builder
    public static class FridgeIngredient {
        private Long memberIngredientId;
        private String name;
        private String storageType;
        private LocalDate expiryDate;
        private Double weight;
        private String unit;

        // 엔티티 -> DTO 변환 메서드
        public static FridgeIngredient from(MemberIngredient entity) {
            return FridgeIngredient.builder()
                    .memberIngredientId(entity.getId())
                    .name(entity.getIngredient().getName())
                    .storageType(entity.getStorageType().toString())
                    .expiryDate(entity.getExpireDate())
                    .weight(entity.getWeight())
                    .unit(entity.getIngredient().getUnit().getLabel())
                    .build();
        }
    }

    //3-2. 냉장고 재료 리스트
    @Getter
    @Builder
    public static class FridgeIngredientList {
        private List<FridgeIngredient> fridgeList;
    }
}
