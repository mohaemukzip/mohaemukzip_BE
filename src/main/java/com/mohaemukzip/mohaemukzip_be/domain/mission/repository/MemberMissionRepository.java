package com.mohaemukzip.mohaemukzip_be.domain.mission.repository;

import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface MemberMissionRepository extends JpaRepository<MemberMission,Long> {

    boolean existsByMemberIdAndMissionIdAndIsCompletedTrue(Long memberId, Long id);
}
