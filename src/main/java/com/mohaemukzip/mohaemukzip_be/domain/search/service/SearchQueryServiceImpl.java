package com.mohaemukzip.mohaemukzip_be.domain.search.service;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeProjection;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.search.converter.SearchConverter;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResponseDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional(readOnly = true)
public class SearchQueryServiceImpl implements SearchQueryService {

    private final RecipeRepository recipeRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public SearchQueryServiceImpl(
            RecipeRepository recipeRepository,
            @Qualifier("redisCacheTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.recipeRepository = recipeRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SearchResponseDTO search(String keyword, Integer page) {
        // 1. Input Validation & Sanitization
        if (keyword == null || keyword.trim().length() < 1) {
            return SearchConverter.toEmptySearchResponseDTO();
        }

        String sanitizedKeyword = keyword.trim();
        // 특수문자 제거 등 추가 정제 로직이 필요하다면 여기에 위치
        // 현재는 공백 제거만 수행 (DB 쿼리에서 REPLACE로 처리하므로 여기선 trim만)
        String strippedKeyword = sanitizedKeyword.replace(" ", "");

        // 2. Redis Caching Strategy
        // 키워드 + 페이지 번호를 조합하여 캐시 키 생성
        String cacheKey = "search::" + strippedKeyword + "::" + page;

        try {
            SearchResponseDTO cachedData = (SearchResponseDTO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return cachedData;
            }
        } catch (Exception e) {
            // Redis 조회 실패 시 로그 남기고 DB 조회 진행 (서비스 중단 방지)
        }

        // 3. DB Query with Projection & Pagination
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        Page<RecipeProjection> resultPage = recipeRepository.findProjectedByTitleContaining(strippedKeyword, pageable);

        SearchResponseDTO response = SearchConverter.toSearchResponseDTO(resultPage);

        // 4. Cache Update (TTL 30분)
        try {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(30));
        } catch (Exception e) {
            // Redis 저장 실패 시 무시
        }

        return response;
    }
}
