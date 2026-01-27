package com.mohaemukzip.mohaemukzip_be.domain.home.service;

import com.mohaemukzip.mohaemukzip_be.domain.home.converter.HomeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.Mission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.repository.MemberMissionRepository;
import com.mohaemukzip.mohaemukzip_be.domain.mission.repository.MissionRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.CookingRecord;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.CookingRecordRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.service.LevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import static com.mohaemukzip.mohaemukzip_be.domain.mission.converter.MissionConverter.toMemberMission;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final MemberRepository memberRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final MissionRepository missionRepository;
    private final RecipeRepository recipeRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final LevelService levelService;

    @Transactional(readOnly = true)
    public HomeResponseDTO getHome(Long memberId) {
        // 1. Member 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));
        // 2. 이달의 집밥 횟수
        Long monthlyCooking = cookingRecordRepository.countMonthlyCooking(memberId);

        // 3. 연속 집밥 일수
        Integer consecutiveDays = calculateConsecutiveDays(memberId);

        // 4. 주간 집밥 기록
        HomeResponseDTO.WeeklyCookingDto weeklyCooking = getWeeklyCooking(memberId);

        // 5. 오늘의 미션
        HomeResponseDTO.TodayMissionDto todayMission = getTodayMission(memberId);

        // 6. 추천 레시피 (조회수 높은 순 5개 => 일단 간단한 버전으로 확인용 => 추후 추천기준에 따른 로직으로 변경할것임)
        List<HomeResponseDTO.RecommendedRecipeDto> recommendedRecipes = getRecommendedRecipes();

        // 7. 다음 레벨까지 필요한 점수
        LevelService.LevelProgressDto levelProgress = levelService.calculateLevelProgress(member.getScore());

        return HomeConverter.toHomeResponseDTO(
                levelProgress,
                monthlyCooking.intValue(),
                member.getScore(),
                consecutiveDays,
                weeklyCooking,
                todayMission,
                recommendedRecipes
        );
    }

    // 연속 집밥 일수 계산 (이번 주 범위 내에서만)
    private Integer calculateConsecutiveDays(Long memberId) {
        // 이번 주 월요일 00:00:00
        LocalDateTime weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        // 오늘 23:59:59
        LocalDateTime weekEnd = LocalDate.now().atTime(23, 59, 59);

        // 이번 주 기록만 조회
        List<CookingRecord> weeklyRecords = cookingRecordRepository
                .findWeeklyCookingRecords(memberId, weekStart, weekEnd);

        if (weeklyRecords.isEmpty()) {
            return 0;
        }

        // 날짜별로 그룹화 (하루에 여러 번 요리해도 1일로 처리)
        List<LocalDate> uniqueDates = weeklyRecords.stream()
                .map(record -> record.getCreatedAt().toLocalDate())
                .distinct()
                .sorted((d1, d2) -> d2.compareTo(d1)) // 최신 날짜부터 (내림차순)
                .toList();

        if (uniqueDates.isEmpty()) {
            return 0;
        }

        // 오늘부터 역순으로 연속 일수 계산
        int consecutiveDays = 0;
        LocalDate today = LocalDate.now();

        // 오늘 기록이 없으면 어제부터 체크 시작
        LocalDate checkDate = uniqueDates.contains(today) ? today : today.minusDays(1);

        for (LocalDate recordDate : uniqueDates) {
            if (recordDate.equals(checkDate)) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break; // 연속이 끊기면 종료
            }
        }
        return consecutiveDays;
    }

    // 주간 집밥 기록
    private HomeResponseDTO.WeeklyCookingDto getWeeklyCooking(Long memberId) {
        // 이번 주 월요일 00:00:00
        LocalDateTime weekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();

        // 이번 주 일요일 23:59:59
        LocalDateTime weekEnd = LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(23, 59, 59);

        List<CookingRecord> weeklyRecords = cookingRecordRepository
                .findWeeklyCookingRecords(memberId, weekStart, weekEnd);

        // 요일별로 그룹화
        Map<DayOfWeek, Boolean> cookingByDay = weeklyRecords.stream()
                .collect(Collectors.toMap(
                        record -> record.getCreatedAt().getDayOfWeek(),
                        record -> true,
                        (existing, replacement) -> existing
                ));

        return HomeConverter.toWeeklyCookingDto(cookingByDay);
    }

    // 오늘의 미션 (도전완료까지 진행 안한 미션 중 하나를 랜덤하게 보여줌)
    private HomeResponseDTO.TodayMissionDto getTodayMission(Long memberId) {

        LocalDate today = LocalDate.now();

        // 오늘 미션 확보(없으면 할당) -> write 트랜잭션 메서드로 분리
        MemberMission memberMission = getOrAssignTodayMission(memberId, today);
        
        return HomeConverter.toTodayMissionDto(memberMission);
    }


    private MemberMission assignTodayMission(Long memberId, LocalDate today) {

        List<Mission> candidates = missionRepository.findAll()
                .stream()
                .filter(mission ->
                        !memberMissionRepository.existsByMemberIdAndMissionId(
                                memberId,
                                mission.getId()
                        )
                )
                .toList();

        if (candidates.isEmpty()) {
            throw new BusinessException(ErrorStatus.NO_AVAILABLE_MISSION);
        }

        Mission selected = candidates.get(new Random().nextInt(candidates.size()));

        return memberMissionRepository.save(
                toMemberMission(memberId, selected, today)
        );
    }

    // 추천 레시피
    private List<HomeResponseDTO.RecommendedRecipeDto> getRecommendedRecipes() {
        List<Recipe> recipes = recipeRepository.findTop5ByOrderByViewsDesc();

        return HomeConverter.toRecommendedRecipeDtos(recipes);
    }

    @Transactional
    protected MemberMission getOrAssignTodayMission(Long memberId, LocalDate today) {
        return memberMissionRepository.findByMemberIdAndAssignedDate(memberId, today)
                .orElseGet(() -> assignTodayMission(memberId, today));
    }
}
