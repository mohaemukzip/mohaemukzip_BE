package com.mohaemukzip.mohaemukzip_be.domain.Recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Recipe.entity.Member_recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRecipeRepository extends JpaRepository<Member_recipe,Long> {
}
