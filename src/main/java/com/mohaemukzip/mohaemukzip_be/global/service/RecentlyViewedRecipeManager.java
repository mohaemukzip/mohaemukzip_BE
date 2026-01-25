package com.mohaemukzip.mohaemukzip_be.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecentlyViewedRecipeManager {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "recent-view:";
    private static final int MAX_SIZE = 30;
    private static final long TTL_DAYS = 30;

    /**
     * Redis Key 생성
     */
    private String getKey(Long memberId) {
        return KEY_PREFIX + memberId;
    }

    /**
     * 레시피 조회 기록 추가
     */
    public void add(Long memberId, Long recipeId) {
        try {
            String key = getKey(memberId);
            String recipeIdStr = recipeId.toString();

            redisTemplate.opsForZSet().remove(key, recipeIdStr);

            double score = System.currentTimeMillis(); // 현재 timestamp를 score로 사용
            redisTemplate.opsForZSet().add(key, recipeId.toString(), score);

            redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS);

            // 최대 개수 초과 시 오래된 항목 제거
            Long size = redisTemplate.opsForZSet().zCard(key);
            if (size != null && size > MAX_SIZE) {
                redisTemplate.opsForZSet().removeRange(key, 0, size - MAX_SIZE - 1);
            }

        } catch (Exception e) {
            log.error("Redis SortedSet Error - memberId={}, recipeId={}", memberId, recipeId, e);
        }
    }

    /**
     * 최근 본 레시피 목록 조회 (최신순)
     */
    public List<Long> getList(Long memberId, int limit) {
        try {
            String key = getKey(memberId);

            // ZREVRANGE: 최신순으로 limit개 조회
            Set<String> recipeIds = redisTemplate.opsForZSet()
                    .reverseRange(key, 0, limit - 1);

            if (recipeIds == null || recipeIds.isEmpty()) {
                return Collections.emptyList();
            }

            return recipeIds.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Redis SortedSet Get Error - memberId={}", memberId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 최근 본 레시피 목록 조회
     */
    public List<Long> getListMypage(Long memberId) {
        return getList(memberId, 3);
    }

    public List<Long> getList(Long memberId) {
        return getList(memberId, MAX_SIZE);
    }

    /**
     * 특정 레시피 제거
     */
    public void remove(Long memberId, Long recipeId) {
        try {
            String key = getKey(memberId);
            redisTemplate.opsForZSet().remove(key, recipeId.toString());
        } catch (Exception e) {
            log.error("Redis SortedSet Remove Error - memberId={}, recipeId={}", memberId, recipeId, e);
        }
    }

    /**
     * 전체 삭제
     */
    public void clear(Long memberId) {
        try {
            String key = getKey(memberId);
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis SortedSet Clear Error - memberId={}", memberId, e);
        }
    }

}
