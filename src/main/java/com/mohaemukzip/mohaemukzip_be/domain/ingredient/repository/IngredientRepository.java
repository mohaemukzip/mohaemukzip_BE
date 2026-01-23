package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    //검색어로 조회 (띄어쓰기 무시)
    @Query("SELECT i FROM Ingredient i WHERE REPLACE(i.name, ' ', '') LIKE CONCAT('%', :name, '%')")
    List<Ingredient> findByNameContaining(@Param("name") String name);

    //카테고리로 조회
    List<Ingredient> findByCategory(Category category);

    //검색어로 조회 + 카테고리로 조회 (동시에)
    List<Ingredient> findByNameContainingAndCategory(String name, Category category);

    // 모든 재료 이름 조회 (Gemini 프롬프트용)
    @Query("SELECT i.name FROM Ingredient i")
    List<String> findAllNames();

    // 재료명으로 조회 (매칭용)
    Optional<Ingredient> findByName(String name);
}
