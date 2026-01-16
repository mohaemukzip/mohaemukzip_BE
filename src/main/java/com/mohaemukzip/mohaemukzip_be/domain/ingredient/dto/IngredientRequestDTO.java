package com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.StorageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;


public class IngredientRequestDTO {

    //냉장고 재료 추가
    @Getter
    @Builder
    public static class AddFridge {
        @NotNull
        private Long ingredientId;
        @NotNull
        private StorageType storageType;
        private LocalDate expireDate;
        @Positive
        private Double weight;
    }
}
