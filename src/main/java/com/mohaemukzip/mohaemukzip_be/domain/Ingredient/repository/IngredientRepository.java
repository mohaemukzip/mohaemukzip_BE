package com.mohaemukzip.mohaemukzip_be.domain.Ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}
