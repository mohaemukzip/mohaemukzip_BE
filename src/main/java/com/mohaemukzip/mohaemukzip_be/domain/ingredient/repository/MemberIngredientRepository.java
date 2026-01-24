package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberIngredientRepository extends JpaRepository<MemberIngredient, Long> {


    @Query("SELECT mi FROM MemberIngredient mi JOIN FETCH mi.ingredient WHERE mi.member.id = :memberId")
    List<MemberIngredient> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT mi FROM MemberIngredient mi WHERE mi.id = :id AND mi.member.id = :memberId")
    Optional<MemberIngredient> findByIdAndMemberId(@Param("id") Long memberIngredientId, @Param("memberId") Long memberId);

    // 유저가 가지고 있는 재료들 중 해당 재료가 있는가 판단 (재료 하나당 MemberIngredient 조회 -> N+1 문제 발생 가능성)
    @Query("select mi.ingredient.id " +
            "from MemberIngredient mi " +
            "where mi.member.id = :memberId " +
            "and mi.ingredient.id in :ingredientIds")
    Set<Long> findIngredientIdsByMemberIdAndIngredientIdIn(
            @Param("memberId") Long memberId,
            @Param("ingredientIds") List<Long> ingredientIds
    );
    // 챗봇 추천용: 유통기한 임박한 순서로 조회 (NULL은 맨 뒤로)
    @Query("SELECT mi FROM MemberIngredient mi JOIN FETCH mi.ingredient WHERE mi.member.id = :memberId ORDER BY CASE WHEN mi.expireDate IS NULL THEN 1 ELSE 0 END, mi.expireDate ASC")
    List<MemberIngredient> findAllByMemberIdOrderByExpireDateAsc(@Param("memberId") Long memberId);
}
