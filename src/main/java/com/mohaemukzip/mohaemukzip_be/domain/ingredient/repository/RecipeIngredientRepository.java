package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    @Query("select ri from RecipeIngredient ri " +
            "join fetch ri.ingredient i " +
            "where ri.recipe.id = :recipeId")
    List<RecipeIngredient> findAllByRecipeId(@Param("recipeId") Long recipeId);

    @Query("SELECT ri.ingredient.name FROM RecipeIngredient ri WHERE ri.recipe.id = :recipeId")
    List<String> findIngredientNamesByRecipeId(@Param("recipeId") Long recipeId);
}
