package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // 모든 재료 이름 조회 (Gemini 프롬프트용)
    @Query("SELECT i.name FROM Ingredient i")
    List<String> findAllNames();

    // 재료명으로 조회 (매칭용)
    List<Ingredient> findAllByNameIn(List<String> names);

    @Query("""
SELECT i FROM Ingredient i
WHERE (:keyword IS NULL OR REPLACE(i.name, ' ', '') LIKE CONCAT('%', REPLACE(:keyword, ' ', ''), '%'))
AND (:category IS NULL OR i.category = :category)
""")
    Page<Ingredient> findByKeywordAndCategory(
            @Param("keyword") String keyword,
            @Param("category") Category category,
            Pageable pageable
    );
}
