package com.mohaemukzip.mohaemukzip_be.domain.home.converter;

import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.enums.MissionStatus;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.Mission;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.global.service.LevelService;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

public class HomeConverter {

    public static HomeResponseDTO toHomeResponseDTO(
            LevelService.LevelProgressDto levelProgress,
            int monthlyCooking,
            int score,
            int consecutiveDays,
            HomeResponseDTO.WeeklyCookingDto weeklyCooking,
            HomeResponseDTO.TodayMissionDto todayMission,
            List<HomeResponseDTO.RecommendedRecipeDto> recommendedRecipes
    ) {
        return HomeResponseDTO.builder()
                .level(levelProgress.currentLevel())
                .title(levelProgress.title())
                .monthlyCooking(monthlyCooking)
                .score(score)
                .nextLevelScore(levelProgress.nextLevelScore())
                .consecutiveDays(consecutiveDays)
                .weeklyCooking(weeklyCooking)
                .todayMission(todayMission)
                .recommendedRecipes(recommendedRecipes)
                .build();
    }

    public static HomeResponseDTO.TodayMissionDto toTodayMissionDto(MemberMission memberMission)  {
        Mission mission = memberMission.getMission();
        return HomeResponseDTO.TodayMissionDto.builder()
                .missionId(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .reward(mission.getReward())
                .keyword(mission.getKeyword())
                .status(memberMission.getStatus().name())
                .build();
    }

    public static HomeResponseDTO.WeeklyCookingDto toWeeklyCookingDto(Map<DayOfWeek, Boolean> cookingByDay) {
        return HomeResponseDTO.WeeklyCookingDto.builder()
                .monday(cookingByDay.getOrDefault(DayOfWeek.MONDAY, false))
                .tuesday(cookingByDay.getOrDefault(DayOfWeek.TUESDAY, false))
                .wednesday(cookingByDay.getOrDefault(DayOfWeek.WEDNESDAY, false))
                .thursday(cookingByDay.getOrDefault(DayOfWeek.THURSDAY, false))
                .friday(cookingByDay.getOrDefault(DayOfWeek.FRIDAY, false))
                .saturday(cookingByDay.getOrDefault(DayOfWeek.SATURDAY, false))
                .sunday(cookingByDay.getOrDefault(DayOfWeek.SUNDAY, false))
                .build();
    }

    public static HomeResponseDTO.RecommendedRecipeDto toRecommendedRecipeDto(Recipe recipe) {
        return HomeResponseDTO.RecommendedRecipeDto.builder()
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .videoId(recipe.getVideoId())
                .videoUrl(recipe.getVideoUrl())
                .imageUrl(recipe.getImageUrl())
                .channel(recipe.getChannel())
                .channelId(recipe.getChannelId())
                .views(recipe.getViews())
                .time(recipe.getTime())
                .cookingTime(recipe.getCookingTime())
                .build();
    }

    public static List<HomeResponseDTO.RecommendedRecipeDto> toRecommendedRecipeDtos(List<Recipe> recipes) {
        return recipes.stream().map(HomeConverter::toRecommendedRecipeDto).toList();
    }



}
