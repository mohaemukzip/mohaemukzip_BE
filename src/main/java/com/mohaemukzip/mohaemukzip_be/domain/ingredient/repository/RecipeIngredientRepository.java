package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    @Query("select ri from RecipeIngredient ri " +
            "join fetch ri.ingredient i " +
            "where ri.recipe.id = :recipeId")
    List<RecipeIngredient> findAllByRecipeId(Long recipeId);

}
