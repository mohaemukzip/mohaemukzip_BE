package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberIngredientRepository extends JpaRepository<MemberIngredient, Long> {


    @Query("SELECT mi FROM MemberIngredient mi JOIN FETCH mi.ingredient WHERE mi.member.id = :memberId")
    List<MemberIngredient> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT mi FROM MemberIngredient mi JOIN FETCH mi.ingredient WHERE mi.id = :id AND mi.member.id = :memberId")
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

    // 레시피 재료들 중 포함 재료들 expiredDate 조회용
    @Query("select mi from MemberIngredient mi " +
            "where mi.member.id = :memberId and mi.ingredient.id in :ingredientIds")
    List<MemberIngredient> findAllByMemberIdAndIngredientIdIn(
            @Param("memberId") Long memberId,
            @Param("ingredientIds") List<Long> ingredientIds
    );

    // 어제 만료된 재료 개수 (스케줄러용)
    @Query("SELECT COUNT(mi) FROM MemberIngredient mi " +
            "WHERE mi.member.id = :memberId AND mi.expireDate = :yesterday")
    int countExpiredYesterday(@Param("memberId") Long memberId, @Param("yesterday") LocalDate yesterday);

    // 최근 7일간 만료된 재료 존재 여부
    @Query("SELECT COUNT(mi) FROM MemberIngredient mi " +
            "WHERE mi.member.id = :memberId AND mi.expireDate BETWEEN :startDate AND :endDate")
    int countExpiredBetween(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
