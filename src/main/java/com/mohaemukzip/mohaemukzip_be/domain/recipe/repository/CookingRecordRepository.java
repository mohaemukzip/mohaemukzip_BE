package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.CookingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CookingRecordRepository extends JpaRepository<CookingRecord, Long> {

    // 이달의 집밥 횟수
    @Query("SELECT COUNT(c) FROM CookingRecord c " +
            "WHERE c.member.id = :memberId " +
            "AND YEAR(c.createdAt) = YEAR(CURRENT_DATE) " +
            "AND MONTH(c.createdAt) = MONTH(CURRENT_DATE)")
    Long countMonthlyCooking(@Param("memberId") Long memberId);

    // CookingRecordRepository.java
    @Query("SELECT DISTINCT FUNCTION('DAYOFWEEK', c.createdAt) as dayOfWeek " +
            "FROM CookingRecord c " +
            "WHERE c.member.id = :memberId " +
            "AND c.createdAt BETWEEN :weekStart AND :weekEnd")
    List<Integer> findWeeklyCookingDays(
            @Param("memberId") Long memberId,
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd
    );

    //특정 기간 동안 집밥 횟수 조회
    long countByMember_IdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    //특정 기간 동안 요리한 날짜 목록 조회
    @Query("SELECT DISTINCT FUNCTION('DATE', c.createdAt) " +
            "FROM CookingRecord c " +
            "WHERE c.member.id = :memberId " +
            "AND c.createdAt >= :start AND c.createdAt < :end " +
            "ORDER BY FUNCTION('DATE', c.createdAt) DESC")
    List<LocalDate> findDistinctCookingDatesBetween(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
