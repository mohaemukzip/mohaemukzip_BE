package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    List<Recipe> findByTitleContaining(String keyword);

    // 랜덤 레시피 조회 (MySQL 전용)
    @Query(value = "SELECT * FROM recipes ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<Recipe> findRandomRecipes(@Param("limit") int limit);

    // 조회수(views) 내림차순으로 상위 5개 조회 (HomeService 사용)
    List<Recipe> findTop5ByOrderByViewsDesc();
}
