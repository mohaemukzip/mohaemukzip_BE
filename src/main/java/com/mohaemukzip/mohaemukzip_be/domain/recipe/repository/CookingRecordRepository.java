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

    // 이번 주 집밥 기록 조회 (해당 주 월요일 00:00 ~ 일요일 23:59)
    @Query("SELECT c FROM CookingRecord c " +
            "WHERE c.member.id = :memberId " +
            "AND c.createdAt >= :weekStart " +
            "AND c.createdAt <= :weekEnd " +
            "ORDER BY c.createdAt DESC")
    List<CookingRecord> findWeeklyCookingRecords(
            @Param("memberId") Long memberId,
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd
    );

    boolean existsByMember_IdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);

    @Query("select distinct function('date', c.createdAt) " +
            "from CookingRecord c " +
            "where c.member.id = :memberId and c.createdAt between :start and :end")
    List<LocalDate> findDistinctCookingDatesBetween(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
