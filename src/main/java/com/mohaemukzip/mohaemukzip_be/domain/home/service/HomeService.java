package com.mohaemukzip.mohaemukzip_be.domain.home.service;

import com.mohaemukzip.mohaemukzip_be.domain.home.converter.HomeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final MemberRepository memberRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final MissionRepository missionRepository;
    private final RecipeRepository recipeRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final LevelService levelService;


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

    // 오늘의 미션 (현재는 도전완료까지 진행 안한 미션 중 하나를 랜덤하게 보여줌. 같은 날짜, 같은 사용자에겐 하루동안 지속하고
    // 사용자마다 동일하지않음.
    private HomeResponseDTO.TodayMissionDto getTodayMission(Long memberId) {

        // 1. 전체 미션 조회
        List<Mission> allMissions = missionRepository.findAll();

        if (allMissions.isEmpty()) return null;

        // 2. 날짜 + 멤버ID 기반 시드
        long seed = LocalDate.now().toEpochDay() + memberId;
        Random random = new Random(seed);

        // 3. 전체 미션 중 하나 선택
        int index = random.nextInt(allMissions.size());
        Mission mission = allMissions.get(index);

        // 4. 선택된 미션이 완료되었는지 여부 DTO에 담아 보내기 (완료된 미션은 프론트에서 따로 표시해야하기 떄문에 추가함)
        boolean isDone = memberMissionRepository.existsByMemberIdAndMissionId(memberId, mission.getId());

        return HomeConverter.toTodayMissionDto(mission, isDone);
    }

    // 추천 레시피
    private List<HomeResponseDTO.RecommendedRecipeDto> getRecommendedRecipes() {
        List<Recipe> recipes = recipeRepository.findTop5ByOrderByViewsDesc();

        return HomeConverter.toRecommendedRecipeDtos(recipes);
    }
}
