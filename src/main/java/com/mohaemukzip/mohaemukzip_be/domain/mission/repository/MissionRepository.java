package com.mohaemukzip.mohaemukzip_be.domain.mission.repository;

import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface MissionRepository extends JpaRepository<Mission,Long> {
    @Query("SELECT m FROM Mission m " +
            "WHERE m.id NOT IN (" +
            "    SELECT mm.mission.id FROM MemberMission mm " +
            "    WHERE mm.member.id = :memberId " +
            "    AND mm.status = 'COMPLETED'" +
            ")")
    List<Mission> findAvailableMissions(@Param("memberId") Long memberId);
}