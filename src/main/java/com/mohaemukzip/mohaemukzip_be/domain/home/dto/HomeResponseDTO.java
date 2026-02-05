package com.mohaemukzip.mohaemukzip_be.domain.home.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeResponseDTO {

    private Integer level;
    private String title;
    private Integer monthlyCooking;
    private Integer score;
    private Integer nextLevelScore;
    private Integer consecutiveDays;
    private String nickname;
    private WeeklyCookingDto weeklyCooking;
    private TodayMissionDto todayMission;
    private List<RecommendedRecipeDto> recommendedRecipes;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class WeeklyCookingDto {
        private Boolean monday;
        private Boolean tuesday;
        private Boolean wednesday;
        private Boolean thursday;
        private Boolean friday;
        private Boolean saturday;
        private Boolean sunday;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TodayMissionDto {
        private Long missionId;
        private String title;
        private String description;
        private Integer reward;
        private Long dishId;
        private String status; // "ASSIGNED", "COMPLETED", "FAILED"
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RecommendedRecipeDto {
        private Long recipeId;
        private String title;
        private String videoId;
        private String videoUrl;
        private String imageUrl;
        private String channel;
        private String channelId;
        private Long views;
        private String time; // "10:54" (영상 길이)
        private Integer cookingTime; // 15 (조리 시간, 분 단위)
    }
}
