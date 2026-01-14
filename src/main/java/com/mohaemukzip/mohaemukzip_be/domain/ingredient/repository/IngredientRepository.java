package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    //검색어로 조회
    List<Ingredient> findByNameContaining(String name);

    //카테고리로 조회
    List<Ingredient> findByCategory(Category category);

    //검색어로 조회 + 카테고리로 조회 (동시에)
    List<Ingredient> findByNameContainingAndCategory(String name, Category category);
}
