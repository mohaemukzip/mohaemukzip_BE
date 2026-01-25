package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.MemberRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MemberRecipeRepository extends JpaRepository<MemberRecipe,Long> {

    //레시피 1건에 대해서 사용자가 저장한 레시피인지 여부 판단
    boolean existsByMember_IdAndRecipe_Id(Long memberId, Long recipeId);

    @Query("SELECT mr.recipe.id FROM MemberRecipe mr WHERE mr.member.id = :memberId AND mr.recipe.id IN :recipeIds")
    Set<Long> findBookmarkedRecipeIdsByMemberId(@Param("memberId") Long memberId, @Param("recipeIds") List<Long> recipeIds);
}
