package com.mohaemukzip.mohaemukzip_be.domain.home.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeCalendarResponseDTO {
    private Integer year;
    private Integer month;
    private List<Integer> cookedDates;                    // 요리한 날짜 목록
    private Map<Integer, List<CookingRecordItem>> cookingRecords;  // 날짜별 요리 기록

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CookingRecordItem {
        private Long recipeId;
        private String imageUrl;
        private String title;
        private String channel;
        private Long views;
        private String time;       // 영상 길이 "10:54"
        private Integer rating;    // 사용자가 준 별점
    }
}
