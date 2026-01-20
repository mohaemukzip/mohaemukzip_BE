package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe,Long> {

    List<Recipe> findTop5ByOrderByViewsDesc();

    @Query("SELECT r FROM Recipe r WHERE REPLACE(r.title, ' ', '') LIKE CONCAT('%', :keyword, '%')")
    List<Recipe> findByTitleContaining(@Param("keyword") String keyword);
}
