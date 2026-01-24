package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.MemberRecipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MemberRecipeRepository extends JpaRepository<MemberRecipe,Long> {
    @Query("SELECT mr.recipe.id FROM MemberRecipe mr WHERE mr.member = :member AND mr.recipe.id IN :recipes")
    Set<Long> findBookmarkedRecipeIds(@Param("member") Member member, @Param("recipeIds") List<Long> recipeIds);
}
