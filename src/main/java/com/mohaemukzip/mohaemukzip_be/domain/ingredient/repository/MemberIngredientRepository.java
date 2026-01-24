package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberIngredientRepository extends JpaRepository<MemberIngredient, Long> {


    @Query("SELECT mi FROM MemberIngredient mi JOIN FETCH mi.ingredient WHERE mi.member.id = :memberId")
    List<MemberIngredient> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT mi FROM MemberIngredient mi WHERE mi.id = :id AND mi.member.id = :memberId")
    Optional<MemberIngredient> findByIdAndMemberId(@Param("id") Long memberIngredientId, @Param("memberId") Long memberId);

    // 챗봇 추천용: 유통기한 임박한 순서로 조회 (NULL은 맨 뒤로)
    @Query("SELECT mi FROM MemberIngredient mi JOIN FETCH mi.ingredient WHERE mi.member.id = :memberId ORDER BY CASE WHEN mi.expireDate IS NULL THEN 1 ELSE 0 END, mi.expireDate ASC")
    List<MemberIngredient> findAllByMemberIdOrderByExpireDateAsc(@Param("memberId") Long memberId);
}
