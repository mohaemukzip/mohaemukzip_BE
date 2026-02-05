package com.mohaemukzip.mohaemukzip_be.global.service;

import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class LevelService {
    public static final int MAX_LEVEL = 4;
    public static final int MIN_LEVEL = 0;

    // 레벨별 필요 점수 (누적)
    private static final int[] LEVEL_SCORES = {0, 50, 200, 475, 825, Integer.MAX_VALUE};

    private static final Map<Integer, String> LEVEL_TITLE_MAP = Map.of(
            0, "집밥 왕초보",
            1, "집밥 입문자",
            2, "집밥 적응 중",
            3, "집밥 루티너",
            4, "집밥계의 고수"
    );

    public record LevelProgressDto(
            int currentLevel,
            String title,
            int currentScore,
            int nextLevelScore,
            int remainingScore
    ) {
        //유효성 검증
        public LevelProgressDto {
            if (currentLevel < MIN_LEVEL || currentLevel > MAX_LEVEL) {
                throw new BusinessException(ErrorStatus.INVALID_LEVEL);
            }
        }

        /**
         * 최대 레벨 도달 여부
         */
        public boolean isMaxLevel() {
            return currentLevel == MAX_LEVEL;
        }
    }

    /**
     * 현재 점수로 레벨 진행 상태 계산
     */
    public LevelProgressDto calculateLevelProgress(int currentScore) {
        validateScore(currentScore);

        int level = getCurrentLevel(currentScore);
        String title = LEVEL_TITLE_MAP.getOrDefault(level, "집밥 왕초보");
        int nextLevelScore = LEVEL_SCORES[level + 1];

        // 최대 레벨 도달
        if (nextLevelScore == Integer.MAX_VALUE) {
            return new LevelProgressDto(
                    level,
                    title,
                    currentScore - LEVEL_SCORES[level],
                    0,
                    0
            );
        }

        int remainingScore = nextLevelScore - currentScore;

        return new LevelProgressDto(
                level,
                title,
                currentScore - LEVEL_SCORES[level],
                nextLevelScore - LEVEL_SCORES[level],
                remainingScore
        );
    }

    /**
     * 점수로 현재 레벨 계산
     */
    public int getCurrentLevel(int score) {
        for (int i = 0; i < LEVEL_SCORES.length - 1; i++) {
            if (score < LEVEL_SCORES[i + 1]) {
                return i;
            }
        }
        return MAX_LEVEL;
    }

    /**
     * 레벨업 여부 확인
     */
    public boolean shouldLevelUp(int oldScore, int newScore) {
        validateScore(oldScore);
        validateScore(newScore);
        return getCurrentLevel(oldScore) < getCurrentLevel(newScore);
    }

    /**
     * 레벨별 필요 점수 조회
     */
    public int getScoreRequiredForLevel(int level) {
        validateLevel(level);
        return LEVEL_SCORES[level];
    }

    /**
     * 레벨별 칭호 조회
     */
    public String getLevelTitle(int level) {
        validateLevel(level);
        return LEVEL_TITLE_MAP.getOrDefault(level, "집밥 왕초보");
    }

    // 유효성 검증
    private void validateScore(int score) {
        if (score < 0) {
            throw new BusinessException(ErrorStatus.INVALID_SCORE);
        }
    }

    private void validateLevel(int level) {
        if (level < MIN_LEVEL || level > MAX_LEVEL) {
            throw new BusinessException(ErrorStatus.INVALID_LEVEL);
        }
    }
}