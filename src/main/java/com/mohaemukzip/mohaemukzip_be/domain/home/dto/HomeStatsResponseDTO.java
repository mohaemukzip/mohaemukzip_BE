package com.mohaemukzip.mohaemukzip_be.domain.home.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeStatsResponseDTO {
    private Integer fridgeScore;           // 냉장고 점수 (0~100)
    private Long totalCookingCount;        // 누적 요리 횟수
    private Double averageDifficulty;      // 도전 난이도 (1.0~5.0)
    private List<MonthlyStat> monthlyCookingStats;  // 월별 집밥 횟수

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private Integer month;
        private Long count;
    }
}
