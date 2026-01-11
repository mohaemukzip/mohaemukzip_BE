package com.mohaemukzip.mohaemukzip_be.domain.Ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Ingredient.entity.Recipe_Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<Recipe_Ingredient, Long> {
}
