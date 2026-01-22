package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.RecipeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory, Long> {

    @Query("SELECT rc.recipe FROM RecipeCategory rc WHERE rc.category.id = :categoryId")
    Page<Recipe> findRecipesByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
}
