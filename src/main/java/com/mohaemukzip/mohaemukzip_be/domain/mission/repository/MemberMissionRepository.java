package com.mohaemukzip.mohaemukzip_be.domain.mission.repository;

import com.mohaemukzip.mohaemukzip_be.domain.home.entity.enums.MissionStatus;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;


public interface MemberMissionRepository extends JpaRepository<MemberMission,Long> {

    // 오늘의 퀘스트 할당 조회
    Optional<MemberMission> findByMemberIdAndAssignedDate(Long memberId, LocalDate assignedDate);

    // 특정 미션을 이미 도전한 이력(완료/실패 포함)인지 체크
    boolean existsByMemberIdAndMissionId(Long memberId, Long missionId);

    // 오늘 완료 여부
    boolean existsByMemberIdAndMissionIdAndAssignedDateAndStatus(Long memberId, Long MissionId, LocalDate assignedDate, MissionStatus status);

}
