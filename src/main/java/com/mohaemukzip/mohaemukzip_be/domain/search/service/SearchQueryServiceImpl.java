package com.mohaemukzip.mohaemukzip_be.domain.search.service;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.DishProjection;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.DishRepository;
import com.mohaemukzip.mohaemukzip_be.domain.search.converter.SearchConverter;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SearchQueryServiceImpl implements SearchQueryService {

    private final DishRepository dishRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public SearchQueryServiceImpl(
            DishRepository dishRepository,
            @Qualifier("redisCacheTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.dishRepository = dishRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SearchResponseDTO search(String keyword, Integer page) {

        String sanitizedKeyword = keyword.trim();
        // DB 쿼리에서 REPLACE를 제거했으므로, 검색어 전처리도 단순화할 수 있지만
        // 사용자 입력 실수(중간 공백)를 보정하기 위해 공백 제거는 유지하는 것이 안전함.
        String strippedKeyword = sanitizedKeyword.replace(" ", "");

        String normalizedKeyword = strippedKeyword.toLowerCase();
        
        // 캐시 키 변경 (v3) - 쿼리 최적화(REPLACE 제거) 반영
        String cacheKey = "search::dish::v3::" + normalizedKeyword + "::" + page;
        
        try {
            SearchResponseDTO cachedData = (SearchResponseDTO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                log.info("Cache Hit! key={}", cacheKey);
                return cachedData;
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed. key={}", cacheKey, e);
        }

        log.info("Cache Miss - DB Query. keyword={}, page={}", strippedKeyword, page);

        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by("id").ascending());
        Page<DishProjection> resultPage = dishRepository.findProjectedByNameStartingWith(strippedKeyword, pageable);

        log.info("DB Search Result Count: {}", resultPage.getTotalElements());

        SearchResponseDTO response = SearchConverter.toSearchResponseDTO(resultPage);

        try {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(30));
        } catch (Exception e) {
            log.warn("Redis cache write failed. key={}", cacheKey, e);
        }

        return response;
    }
}
