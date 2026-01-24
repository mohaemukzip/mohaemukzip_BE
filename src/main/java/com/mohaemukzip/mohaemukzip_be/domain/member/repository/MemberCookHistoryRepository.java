package com.mohaemukzip.mohaemukzip_be.domain.member.repository;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.MemberCookHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MemberCookHistoryRepository extends JpaRepository<MemberCookHistory, Long> {

    @Query("SELECT mch FROM MemberCookHistory mch WHERE mch.member.id = :memberId AND mch.cookedAt >= :startDate")
    List<MemberCookHistory> findAllByMemberIdAndCookedAtAfter(@Param("memberId") Long memberId, @Param("startDate") LocalDateTime startDate);
}
