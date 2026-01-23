package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe,Long> {

    List<Recipe> findTop5ByOrderByViewsDesc();

    @Query("SELECT r FROM Recipe r WHERE REPLACE(r.title, ' ', '') LIKE CONCAT('%', :keyword, '%')")
    List<Recipe> findByTitleContaining(@Param("keyword") String keyword);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    // 2명이상 사용자가 한 레시피 동시 조회해서 addRating 하는거 방지용
    @Query("select r from Recipe r where r.id = :recipeId")
    Recipe findByIdForUpdate(@Param("recipeId") Long recipeId);

    // videoId 중복 저장 방지용
    boolean existsByVideoId(String videoId);

}
