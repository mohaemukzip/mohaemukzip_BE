package com.mohaemukzip.mohaemukzip_be.domain.Ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Ingredient.entity.Member_Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberIngredientRepository extends JpaRepository<Member_Ingredient, Long> {
}
