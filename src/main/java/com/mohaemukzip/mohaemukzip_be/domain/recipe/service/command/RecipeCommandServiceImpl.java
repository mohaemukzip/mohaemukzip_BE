package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.RecipeIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.enums.MissionStatus;
import com.mohaemukzip.mohaemukzip_be.domain.mission.repository.MemberMissionRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.*;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeIngredientConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeStepConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.GeminiResponseConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.*;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.crawler.RecipeCrawler;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.service.LevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeCommandServiceImpl implements RecipeCommandService {

    private final RecipeRepository recipeRepository;
    private final DishRepository dishRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final IngredientRepository ingredientRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final MemberMissionRepository memberMissionRepository;
    private final MemberRepository memberRepository;
    private final MemberIngredientRepository memberIngredientRepository;
    private final SummaryRepository summaryRepository;
    private final MemberRecipeRepository memberRecipeRepository;
    private final LevelService levelService;

    private final RecipeConverter recipeConverter;
    private final RecipeIngredientConverter recipeIngredientConverter;
    private final RecipeStepConverter recipeStepConverter;


    // ==========================================================
    // Public API
    // ==========================================================
    @Override
    @Transactional
    public RecipeResponseDTO.CookingRecordCreateResponseDTO createCookingRecord(
            Long memberId,
            Long recipeId,
            int rating
    ) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ErrorStatus.INVALID_RATING);
        }

        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        int oldScore = member.getScore() == null ? 0 : member.getScore();

        Recipe recipe = recipeRepository.findByIdForUpdate(recipeId);
        if (recipe == null) throw new BusinessException(ErrorStatus.RECIPE_NOT_FOUND);
        recipe.addRating(rating);

        CookingRecord record = cookingRecordRepository.save(
                CookingRecord.builder()
                        .member(member)
                        .recipe(recipe)
                        .rating(rating)
                        .build()
        );

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        long todayCookingCount = cookingRecordRepository
                .countByMember_IdAndCreatedAtBetween(memberId, startOfToday, startOfTomorrow);

        boolean isFirstCookingToday = (todayCookingCount == 1);

        int rewardScore = 0;
        rewardScore += 5; // 기본 기록 점수

        Long dishId = recipe.getDish() != null ? recipe.getDish().getId() : null;
        rewardScore += completeTodayMissionIfMatched(memberId, dishId);

        if (isFirstCookingToday) {
            rewardScore += calculateIngredientBonus(memberId, recipeId, today);
            rewardScore += calculateStreakBonus(memberId, today);
        }

        member.addScore(rewardScore);
        int newScore = member.getScore();
        boolean leveledUp = levelService.shouldLevelUp(oldScore, newScore);

        deductMemberIngredients(memberId, recipeId);

        return RecipeResponseDTO.CookingRecordCreateResponseDTO.builder()
                .cookingRecordId(record.getId())
                .recipeId(recipe.getId())
                .rating(rating)
                .recipeLevel(recipe.getLevel())
                .ratingCount(recipe.getRatingCount())
                .rewardScore(rewardScore)
                .leveledUp(leveledUp)
                .build();
    }

    @Override
    @Transactional
    public RecipeResponseDTO.BookmarkToggleResult toggleBookmark(Long memberId, Long recipeId) {
        int deletedCount = memberRecipeRepository.deleteByMemberIdAndRecipeId(memberId, recipeId);

        if (deletedCount > 0) {
            return RecipeConverter.toBookmarkToggleResult(false);
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.RECIPE_NOT_FOUND));

        Member member = memberRepository.getReferenceById(memberId);

        memberRecipeRepository.save(
                MemberRecipe.builder()
                        .member(member)
                        .recipe(recipe)
                        .build()
        );

        return RecipeConverter.toBookmarkToggleResult(true);
    }

    // ==========================================================
    // For Facade (DB Transactions Only)
    // ==========================================================

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllIngredientNames() {
        return ingredientRepository.findAllNames();
    }

    @Override
    @Transactional
    public Long saveRecipeAndIngredients(Long dishId, String videoId, RecipeCrawler.RecipeData data) {
        if (recipeRepository.existsByVideoId(videoId)) {
            throw new BusinessException(ErrorStatus.RECIPE_ALREADY_EXISTS);
        }

        Dish dish = null;
        if (dishId != null) {
            dish = dishRepository.findById(dishId)
                    .orElseThrow(() -> new BusinessException(ErrorStatus.DISH_NOT_FOUND));
        }

        Recipe recipe = saveRecipe(data, dish);
        saveRecipeIngredients(recipe, data.ingredients());
        return recipe.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Recipe getRecipeForSummary(Long recipeId) {
        return recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.RECIPE_NOT_FOUND));
    }

    @Override
    @Transactional
    public Summary tryCreateSummary(Long recipeId) {
        Recipe recipe = getRecipeForSummary(recipeId);
        try {
            return summaryRepository.save(
                    Summary.builder()
                            .recipe(recipe)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            log.info("Summary 동시 생성 감지 (선점 실패) - recipeId: {}", recipe.getId());
            return null; // 누군가 먼저 생성함
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RecipeResponseDTO.SummaryCreateResult getExistingSummaryResult(Long recipeId) {
        Summary existing = summaryRepository.findByRecipeId(recipeId).orElse(null);
        if (existing != null) {
            int count = recipeStepRepository
                    .findAllBySummaryIdOrderByStepNumberAsc(existing.getId())
                    .size();
            return RecipeResponseDTO.SummaryCreateResult.builder()
                    .summaryExists(true)
                    .stepCount(count)
                    .build();
        }
        return RecipeResponseDTO.SummaryCreateResult.builder()
                .summaryExists(false)
                .stepCount(0)
                .build();
    }

    @Override
    @Transactional
    public RecipeResponseDTO.SummaryCreateResult saveSummarySteps(Long summaryId, List<GeminiResponseConverter.StepDraft> steps) {
        Summary summary = summaryRepository.findById(summaryId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.SUMMARY_CREATION_FAILED));
        
        List<RecipeStep> entities = recipeStepConverter.toEntities(summary, steps);
        recipeStepRepository.saveAll(entities);

        return RecipeResponseDTO.SummaryCreateResult.builder()
                .summaryExists(true)
                .stepCount(entities.size())
                .build();
    }

    @Override
    @Transactional
    public void deleteSummary(Long summaryId) {
        summaryRepository.deleteById(summaryId);
    }

    // ==========================================================
    // Private Helpers
    // ==========================================================

    private Recipe saveRecipe(RecipeCrawler.RecipeData data, Dish dish) {
        Recipe recipe = recipeConverter.toEntity(data, dish);

        try {
            return recipeRepository.save(recipe);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorStatus.RECIPE_ALREADY_EXISTS);
        }
    }

    private void saveRecipeIngredients(Recipe recipe, List<RecipeCrawler.IngredientData> ingredientDataList) {
        List<String> ingredientNames = ingredientDataList.stream()
                .map(RecipeCrawler.IngredientData::name)
                .toList();

        List<Ingredient> foundIngredients = ingredientRepository.findAllByNameIn(ingredientNames);

        Map<String, Ingredient> ingredientMap = foundIngredients.stream()
                .collect(Collectors.toMap(Ingredient::getName, Function.identity()));

        List<RecipeIngredient> recipeIngredients = ingredientDataList.stream()
                .map(data -> {
                    Ingredient ingredient = ingredientMap.get(data.name());
                    if (ingredient == null) {
                        log.warn("재료 매칭 실패 - DB에 없음: {}", data.name());
                        return null;
                    }
                    return recipeIngredientConverter.toEntity(recipe, ingredient, data);
                })
                .filter(Objects::nonNull)
                .toList();

        if (!recipeIngredients.isEmpty()) {
            recipeIngredientRepository.saveAll(recipeIngredients);
        }
    }

    private int completeTodayMissionIfMatched(Long memberId, Long dishId) {
        LocalDate today = LocalDate.now();

        MemberMission mm = memberMissionRepository
                .findByMemberIdAndAssignedDateForUpdate(memberId, today)
                .orElse(null);

        if (mm == null) return 0;
        if (mm.getStatus() != MissionStatus.ASSIGNED) return 0;

        Long missionDishId = mm.getMission().getDishId();
        if (missionDishId == null || dishId == null) return 0;

        if (!missionDishId.equals(dishId)) return 0;

        mm.completeToday();

        Integer reward = mm.getMission().getReward();
        return (reward == null) ? 0 : reward;
    }

    private int calculateIngredientBonus(Long memberId, Long recipeId, LocalDate today) {
        List<Long> ingredientIds = recipeIngredientRepository.findIngredientIdsByRecipeId(recipeId);
        if (ingredientIds.isEmpty()) return 0;

        List<MemberIngredient> owned = memberIngredientRepository
                .findAllByMemberIdAndIngredientIdIn(memberId, ingredientIds);

        if (owned.isEmpty()) return 0;

        LocalDate d3 = today.plusDays(3);

        boolean hasD3 = owned.stream()
                .map(MemberIngredient::getExpireDate)
                .filter(Objects::nonNull)
                .anyMatch(exp -> !exp.isBefore(today) && !exp.isAfter(d3));

        return hasD3 ? 10 : 5;
    }

    private int calculateStreakBonus(Long memberId, LocalDate today) {
        LocalDateTime start = today.minusDays(7).atStartOfDay();
        LocalDateTime end = today.minusDays(1).atTime(23,59,59);

        List<LocalDate> cookedDates = cookingRecordRepository
                .findDistinctCookingDatesBetween(memberId, start, end)
                .stream()
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .toList();
        
        Set<LocalDate> set = new HashSet<>(cookedDates);

        int streakBeforeToday = 0;
        LocalDate d = today.minusDays(1);
        while (set.contains(d)) {
            streakBeforeToday++;
            d = d.minusDays(1);
            if (streakBeforeToday >= 6) break;
        }

        int streakIncludingToday = streakBeforeToday + 1;

        if (streakIncludingToday >= 7) return 25;
        if (streakIncludingToday >= 3) return 10;
        return 5;
    }

    private void deductMemberIngredients(Long memberId, Long recipeId) {
        List<RecipeIngredient> recipeIngredients = recipeIngredientRepository.findAllByRecipeId(recipeId);

        if (recipeIngredients.isEmpty()) {
            return;
        }

        List<Long> ingredientIds = recipeIngredients.stream()
                .map(ri -> ri.getIngredient().getId())
                .toList();

        List<MemberIngredient> memberIngredients = memberIngredientRepository
                .findAllByMemberIdAndIngredientIdIn(memberId, ingredientIds);

        if (memberIngredients.isEmpty()) {
            return;
        }

        Map<Long, Double> recipeAmountMap = recipeIngredients.stream()
                .collect(Collectors.toMap(
                        ri -> ri.getIngredient().getId(),
                        ri -> ri.getAmount() != null ? ri.getAmount() : 0.0
                ));

        List<MemberIngredient> toDelete = new ArrayList<>();

        for (MemberIngredient mi : memberIngredients) {
            Long ingredientId = mi.getIngredient().getId();
            Double amountToDeduct = recipeAmountMap.get(ingredientId);

            if (amountToDeduct != null && amountToDeduct > 0) {
                mi.subtractWeight(amountToDeduct);

                if (mi.isEmpty()) {
                    toDelete.add(mi);
                }
            }
        }

        if (!toDelete.isEmpty()) {
            memberIngredientRepository.deleteAll(toDelete);
        }

        log.debug("재료 차감 완료 - memberId: {}, recipeId: {}, 차감: {}, 삭제: {}",
                memberId, recipeId, memberIngredients.size(), toDelete.size());
    }
}