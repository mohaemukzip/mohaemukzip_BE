package com.mohaemukzip.mohaemukzip_be.domain.mission.repository;

import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission,Long> {

    // 완료 안 한 미션 중 오늘의 미션 1개 (사용자별, 일단위 고정)
    @Query(value = "SELECT m.* FROM missions m " +
            "WHERE m.mission_id NOT IN (" +
            "  SELECT mm.mission_id FROM member_missions mm " +
            "  WHERE mm.member_id = :memberId AND mm.is_completed = true" + ") " +
            "ORDER BY RAND(:seed) LIMIT 1", nativeQuery = true)
    Optional<Mission> findDailyMissionForMember(@Param("memberId") Long memberId, @Param("seed") int seed);
}