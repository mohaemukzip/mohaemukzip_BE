package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.crawler.RecipeCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RecipeIngredientConverter {

    public RecipeIngredient toEntity(
            Recipe recipe,
            Ingredient ingredient,
            RecipeCrawler.IngredientData ingredientData
    ) {
        Double amount = parseAmount(ingredientData.amount());

        return RecipeIngredient.builder()
                .recipe(recipe)
                .ingredient(ingredient)
                .amount(amount)
                .build();
    }

    private Double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) {
            return null;
        }

        try {
            return Double.valueOf(amountStr);
        } catch (NumberFormatException e) {
            log.warn("amount 파싱 실패: {}", amountStr);
            return null;
        }
    }
}