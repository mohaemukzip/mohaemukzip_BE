package com.mohaemukzip.mohaemukzip_be.domain.Recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe,Long> {
}
