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
        String strippedKeyword = sanitizedKeyword.replace(" ", "");

        String normalizedKeyword = strippedKeyword.toLowerCase();
        
        // 캐시 키 변경 (v1 추가)하여 기존 캐시 무시 및 갱신 유도
        String cacheKey = "search::dish::v1::" + normalizedKeyword + "::" + page;
        
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
        Page<DishProjection> resultPage = dishRepository.findProjectedByNameContaining(strippedKeyword, pageable);

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
