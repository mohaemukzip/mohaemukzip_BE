package com.mohaemukzip.mohaemukzip_be.domain.home.service.query;

import com.mohaemukzip.mohaemukzip_be.domain.home.converter.HomeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeCalendarResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeStatsResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.service.assignment.MissionAssignmentService;
import com.mohaemukzip.mohaemukzip_be.domain.mission.service.query.MissionQueryService;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.CookingRecord;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.CookingRecordRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.service.LevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeQueryServiceImpl implements HomeQueryService {
    private final MemberRepository memberRepository;

    private final CookingRecordRepository cookingRecordRepository;
    private final RecipeRepository recipeRepository;
    private final MissionAssignmentService missionAssignmentService;
    private final LevelService levelService;
    private final MissionQueryService missionQueryService;

    // 추천 레시피용 Repository
    private final MemberIngredientRepository memberIngredientRepository;

    @Override
    @Transactional(readOnly = true)
    public HomeResponseDTO getHome(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        //레벨 및 점수 계산
        int currentScore = Optional.ofNullable(member.getScore()).orElse(0);
        LevelService.LevelProgressDto levelProgress = levelService.calculateLevelProgress(currentScore);

        int monthlyCooking = calculateMonthlyCooking(memberId);

        String nickname = member.getNickname();

        //연속 요리 일수 계산
        int consecutiveDays = calculateConsecutiveDays(memberId);

        //주간 요리 현황
        HomeResponseDTO.WeeklyCookingDto weeklyCooking = getWeeklyCooking(memberId);

        //오늘의 미션
        HomeResponseDTO.TodayMissionDto todayMission = getTodayMission(memberId);

        //추천 레시피
        List<HomeResponseDTO.RecommendedRecipeDto> recommendedRecipes = getRecommendedRecipes(memberId);

        return HomeConverter.toHomeResponseDTO(
                levelProgress,
                monthlyCooking,
                currentScore,
                nickname,
                consecutiveDays,
                weeklyCooking,
                todayMission,
                recommendedRecipes
        );
    }

    @Override
    public HomeStatsResponseDTO getStats(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        // 냉장고 점수
        Integer fridgeScore = member.getFridgeScore() != null ? member.getFridgeScore() : 100;

        // 누적 요리 횟수
        Long totalCookingCount = cookingRecordRepository.countByMemberId(memberId);

        // 도전 난이도 (평균 rating)
        Double averageDifficulty = cookingRecordRepository.findAverageRatingByMemberId(memberId);

        // 월별 집밥 횟수 (현재 연도)
        int currentYear = LocalDate.now().getYear();
        List<Object[]> monthlyData = cookingRecordRepository.countByMemberIdGroupByMonth(memberId, currentYear);

        // 1~12월 데이터 생성 (없는 월은 0으로)
        Map<Integer, Long> monthMap = new HashMap<>();
        for (Object[] row : monthlyData) {
            Integer month = (Integer) row[0];
            Long count = (Long) row[1];
            monthMap.put(month, count);
        }

        List<HomeStatsResponseDTO.MonthlyStat> monthlyCookingStats = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            monthlyCookingStats.add(HomeConverter.toMonthlyStat(m, monthMap.getOrDefault(m, 0L)));
        }

        return HomeConverter.toHomeStatsResponseDTO(fridgeScore, totalCookingCount, averageDifficulty, monthlyCookingStats);
    }

    @Override
    public HomeCalendarResponseDTO getCalendar(Long memberId, int year, int month) {
        // 요리한 날짜 목록
        List<Integer> cookedDates = cookingRecordRepository
                .findCookedDaysByMemberIdAndYearMonth(memberId, year, month);

        // 날짜별 요리 기록
        Map<Integer, List<HomeCalendarResponseDTO.CookingRecordItem>> cookingRecords = new HashMap<>();

        for (Integer day : cookedDates) {
            LocalDate date = LocalDate.of(year, month, day);
            List<CookingRecord> records = cookingRecordRepository.findByMemberIdAndDate(memberId, date);
            cookingRecords.put(day, HomeConverter.toCookingRecordItems(records));
        }

        return HomeConverter.toHomeCalendarResponseDTO(year, month, cookedDates, cookingRecords);
    }

    /**
     * 월간 요리 횟수 계산 (이번 달)
     */
    private int calculateMonthlyCooking(Long memberId) {
        Long count = cookingRecordRepository.countMonthlyCooking(memberId);

        log.debug("월간 요리 횟수 계산 완료 - memberId: {}, count: {}", memberId, count);

        return count != null ? count.intValue() : 0;
    }

    /**
     * 연속 요리 일수 계산
     */
    private int calculateConsecutiveDays(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.minusDays(30).atStartOfDay();
        LocalDateTime endDate = today.plusDays(1).atStartOfDay();

        // 최근 30일간의 요리 기록 조회 (충분한 범위)
        List<LocalDate> cookingDates = cookingRecordRepository
                .findDistinctCookingDatesBetween(memberId, startOfToday, endDate)
                .stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .toList();

        if (cookingDates == null || cookingDates.isEmpty()) {
            log.debug("연속 요리 일수 계산 완료 - memberId: {}, days: 0 (기록 없음)", memberId);
            return 0;
        }

        Set<LocalDate> cookingDateSet = new HashSet<>(cookingDates);

        // 오늘부터 역순으로 연속된 날짜 카운트
        int consecutive = 0;
        LocalDate checkDate = today;

        while (cookingDateSet.contains(checkDate)) {
            consecutive++;
            checkDate = checkDate.minusDays(1);
        }

        log.debug("연속 요리 일수 계산 완료 - memberId: {}, days: {}", memberId, consecutive);
        return consecutive;
    }

    /**
     * 주간 요리 현황 조회
     */
    private HomeResponseDTO.WeeklyCookingDto getWeeklyCooking(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime weekStartDateTime = weekStart.atStartOfDay();
        LocalDateTime weekEndDateTime = weekEnd.plusDays(1).atStartOfDay();

        List<Integer> cookingDaysNumbers = cookingRecordRepository
                .findWeeklyCookingDays(memberId, weekStartDateTime, weekEndDateTime);

        // 요일별로 그룹핑
        Map<DayOfWeek, Boolean> cookingByDay = cookingDaysNumbers.stream()
                .map(dayNum -> {return dayNum == 1 ? DayOfWeek.SUNDAY : DayOfWeek.of(dayNum - 1);
                })
                .distinct()
                .collect(Collectors.toMap(
                        day -> day,
                        day -> true,
                        (v1, v2) -> v1
                ));

        HomeResponseDTO.WeeklyCookingDto result = HomeConverter.toWeeklyCookingDto(cookingByDay);

        return result;
    }

    /**
     * 오늘의 미션 조회 (없으면 할당)
     */
    private HomeResponseDTO.TodayMissionDto getTodayMission(Long memberId) {
        LocalDate today = LocalDate.now();

        Optional<MemberMission> existing =
                missionQueryService.findTodayMission(memberId, today);

        if (existing.isPresent()) {
            return HomeConverter.toTodayMissionDto(existing.get());
        }

        try {
            // 별도 트랜잭션 서비스를 통해 조회/할당
            MemberMission memberMission = missionAssignmentService
                    .assignTodayMission(memberId, today);

            return HomeConverter.toTodayMissionDto(memberMission);

        } catch (BusinessException e) {
            // 할당 가능한 미션이 없는 경우
            log.warn("오늘의 미션 할당 실패 - memberId: {}, reason: {}",
                    memberId, e.getMessage());
            return null;
        }
    }

    /**
     * 추천 레시피 목록 조회
     * - 신규 사용자: 조회수 상위 5개
     * - 기존 사용자: 2가지 기준으로 후보 수집 후 랜덤 5개 선택
     *   1. 재료 기반 (유통기한 임박/다량 보유/장기 미소진 - 통합 쿼리)
     *   2. 최근 요리 카테고리
     */
    private List<HomeResponseDTO.RecommendedRecipeDto> getRecommendedRecipes(Long memberId) {
        // 1. 신규 사용자 체크 (재료 없음 AND 요리 기록 없음)
        boolean hasIngredients = memberIngredientRepository.existsByMemberId(memberId);
        boolean hasCookingRecords = cookingRecordRepository.existsByMemberId(memberId);

        if (!hasIngredients && !hasCookingRecords) {
            log.debug("신규 사용자 - 조회수 기반 추천 - memberId: {}", memberId);
            List<Recipe> recipes = recipeRepository.findTop5ByOrderByViewsDesc();
            return HomeConverter.toRecommendedRecipeDtos(recipes);
        }

        // 2. 후보 레시피 수집
        Set<Long> candidateRecipeIds = new HashSet<>();

        // 2-1. 재료 기반 추천 (유통기한 임박/다량 보유/장기 미소진 - 통합 쿼리)
        LocalDate today = LocalDate.now();
        LocalDate expireThreshold = today.plusDays(3);
        LocalDateTime unusedThreshold = LocalDateTime.now().minusDays(10);

        List<Long> ingredientBasedRecipeIds = memberIngredientRepository
                .findRecommendedRecipeIds(memberId, today, expireThreshold, unusedThreshold);
        candidateRecipeIds.addAll(ingredientBasedRecipeIds);
        log.debug("재료 기반 레시피 후보 추가 - memberId: {}, count: {}", memberId, ingredientBasedRecipeIds.size());

        // 2-2. 최근 요리 카테고리
        List<CookingRecord> recentRecords = cookingRecordRepository
                .findRecentByMemberId(memberId, PageRequest.of(0, 1));
        if (!recentRecords.isEmpty()) {
            Category recentCategory = recentRecords.get(0).getRecipe().getCategory();
            if (recentCategory != null) {
                List<Long> categoryRecipeIds = recipeRepository.findIdsByCategory(recentCategory);
                candidateRecipeIds.addAll(categoryRecipeIds);
                log.debug("최근 요리 카테고리 기반 레시피 후보 추가 - memberId: {}, category: {}", memberId, recentCategory);
            }
        }

        // 3. 후보가 없으면 기본 추천
        if (candidateRecipeIds.isEmpty()) {
            log.debug("후보 레시피 없음 - 조회수 기반 추천 - memberId: {}", memberId);
            List<Recipe> recipes = recipeRepository.findTop5ByOrderByViewsDesc();
            return HomeConverter.toRecommendedRecipeDtos(recipes);
        }

        // 4. 후보 중 랜덤 5개 선택
        List<Long> candidateList = new ArrayList<>(candidateRecipeIds);
        Collections.shuffle(candidateList);
        List<Long> selectedIds = candidateList.stream().limit(5).toList();

        // 5. 레시피 조회 및 반환
        List<Recipe> recipes = recipeRepository.findByIdIn(selectedIds);
        log.debug("개인화 추천 레시피 조회 완료 - memberId: {}, candidateCount: {}, selectedCount: {}",
                memberId, candidateRecipeIds.size(), recipes.size());

        return HomeConverter.toRecommendedRecipeDtos(recipes);
    }
}