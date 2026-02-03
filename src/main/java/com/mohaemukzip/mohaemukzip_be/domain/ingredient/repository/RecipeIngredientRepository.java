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

    @Query("select ri.ingredient.id from RecipeIngredient ri where ri.recipe.id = :recipeId")
    List<Long> findIngredientIdsByRecipeId(@Param("recipeId") Long recipeId);

    // ===== 홈 화면 추천 레시피용 메서드 =====

    // 특정 재료들을 포함하는 레시피 ID 목록 조회
    @Query("SELECT DISTINCT ri.recipe.id FROM RecipeIngredient ri WHERE ri.ingredient.id IN :ingredientIds")
    List<Long> findRecipeIdsByIngredientIds(@Param("ingredientIds") List<Long> ingredientIds);
}
