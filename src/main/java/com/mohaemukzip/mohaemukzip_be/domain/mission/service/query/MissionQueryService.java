package com.mohaemukzip.mohaemukzip_be.domain.mission.service.query;

import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.repository.MemberMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MissionQueryService {

    private final MemberMissionRepository memberMissionRepository;

    @Transactional(readOnly = true)
    public Optional<MemberMission> findTodayMission(Long memberId, LocalDate today) {
        return memberMissionRepository.findByMemberIdAndAssignedDate(memberId, today);
    }
}
