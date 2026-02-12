package com.mohaemukzip.mohaemukzip_be.domain.mission.service.assignment;

import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.Mission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.repository.MemberMissionRepository;
import com.mohaemukzip.mohaemukzip_be.domain.mission.repository.MissionRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import static com.mohaemukzip.mohaemukzip_be.domain.mission.converter.MissionConverter.toMemberMission;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

// 오늘의 미션 할당
@Slf4j
@Service
@RequiredArgsConstructor
public class MissionAssignmentService {

    private final MissionRepository missionRepository;
    private final MemberMissionRepository memberMissionRepository;

    //오늘의 미션 조회 또는 할당
    @Transactional(propagation = REQUIRES_NEW)
    public MemberMission assignTodayMission(Long memberId, LocalDate today) {
        return assign(memberId, today);
    }

    private MemberMission assign(Long memberId, LocalDate today) {
        List<Mission> candidates = missionRepository.findAvailableMissions(memberId);

        if (candidates.isEmpty()) {
            log.error("할당 가능한 미션 없음 - memberId: {}", memberId);
            throw new BusinessException(ErrorStatus.NO_AVAILABLE_MISSION);
        }

        Mission selected = candidates.get(new Random().nextInt(candidates.size()));

        log.info("미션 할당 완료 - memberId: {}, missionId: {}, title: {}",
                memberId, selected.getId(), selected.getTitle());

        return memberMissionRepository.save(
                toMemberMission(memberId, selected, today)
        );
    }
}