package com.mohaemukzip.mohaemukzip_be.domain.search.service;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.search.converter.SearchConverter;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResultDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class SearchQueryServiceImpl implements SearchQueryService {

    private final RecipeRepository recipeRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public SearchQueryServiceImpl(
            RecipeRepository recipeRepository,
            @Qualifier("redisCacheTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.recipeRepository = recipeRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SearchResponseDTO search(String keyword) {
        // 빈 키워드 방어 로직
        if (keyword == null || keyword.isBlank()) {
            return SearchConverter.toEmptySearchResponseDTO();
        }

        // 공백 제거된 키워드 생성 (DB 검색용)
        String strippedKeyword = keyword.replace(" ", "");

        // Redis 키는 원본 키워드 사용 (또는 strippedKeyword 사용 가능)
        // 여기서는 원본 키워드를 사용하여 사용자 입력 그대로 캐싱
        String cacheKey = "search::" + strippedKeyword; 

        // 1. Redis 캐시 조회
        try {
            SearchResponseDTO cachedData = (SearchResponseDTO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return cachedData;
            }
        } catch (Exception e) {
            // Redis 조회 실패 시 DB 조회 진행
        }

        // 2. DB 조회 (공백 제거된 키워드 사용)
        List<SearchResultDTO> results = new ArrayList<>();

        // 메뉴(레시피) 검색
        results.addAll(searchRecipes(strippedKeyword));

        SearchResponseDTO response = SearchConverter.toSearchResponseDTO(results);

        // 3. Redis 캐시 저장 (TTL 30분)
        try {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(30));
        } catch (Exception e) {
            // Redis 저장 실패 시 무시
        }

        return response;
    }

    // 메뉴(레시피) 검색 로직
    private List<SearchResultDTO> searchRecipes(String keyword) {
        // 중복 제거 및 순서 보장을 위해 LinkedHashMap 사용
        Map<Long, SearchResultDTO> recipeMap = new LinkedHashMap<>();

        // 레시피 제목 검색
        List<Recipe> recipes = recipeRepository.findByTitleContaining(keyword);
        for (Recipe recipe : recipes) {
            recipeMap.put(recipe.getId(), SearchConverter.toSearchResultDTO(recipe));
        }

        return new ArrayList<>(recipeMap.values());
    }
}
