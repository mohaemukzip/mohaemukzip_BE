package com.mohaemukzip.mohaemukzip_be.domain.mission.repository;

import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;


public interface MemberMissionRepository extends JpaRepository<MemberMission,Long> {

    // 오늘의 퀘스트 할당 조회
    Optional<MemberMission> findByMemberIdAndAssignedDate(Long memberId, LocalDate assignedDate);
}
