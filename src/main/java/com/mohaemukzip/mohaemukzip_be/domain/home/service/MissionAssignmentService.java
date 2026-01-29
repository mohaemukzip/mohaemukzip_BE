package com.mohaemukzip.mohaemukzip_be.domain.home.service;

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

// 오늘의 미션 할당 및 조회 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class MissionAssignmentService {

    private final MissionRepository missionRepository;
    private final MemberMissionRepository memberMissionRepository;

    //오늘의 미션 조회 또는 할당
    @Transactional
    public MemberMission getOrAssignTodayMission(Long memberId, LocalDate today) {
        return memberMissionRepository.findByMemberIdAndAssignedDate(memberId, today)
                .orElseGet(() -> {
                    log.info("오늘의 미션 없음, 새로 할당 - memberId: {}", memberId);
                    return assignTodayMission(memberId, today);
                });
    }

    /**
     * 새로운 미션 할당
     * - 아직 완료하지 않은 미션 중 랜덤 선택
     */
    private MemberMission assignTodayMission(Long memberId, LocalDate today) {
        // 할당 가능한 미션 조회 (완료하지 않은 미션만)
        List<Mission> candidates = missionRepository.findAvailableMissions(memberId);

        // 할당 가능한 미션이 없는 경우
        if (candidates.isEmpty()) {
            log.error("할당 가능한 미션 없음 - memberId: {}", memberId);
            throw new BusinessException(ErrorStatus.NO_AVAILABLE_MISSION);
        }

        // 랜덤 선택
        Mission selected = candidates.get(new Random().nextInt(candidates.size()));

        log.info("미션 할당 완료 - memberId: {}, missionId: {}, title: {}",
                memberId, selected.getId(), selected.getTitle());

        // MemberMission 생성 및 저장
        return memberMissionRepository.save(
                toMemberMission(memberId, selected, today)
        );
    }
}