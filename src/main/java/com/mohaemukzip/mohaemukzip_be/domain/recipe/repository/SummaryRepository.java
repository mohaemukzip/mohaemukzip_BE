package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary,Long> {


    boolean existsByRecipeId(Long recipeId);

    Optional<Summary> findByRecipeId(Long recipeId);
}
