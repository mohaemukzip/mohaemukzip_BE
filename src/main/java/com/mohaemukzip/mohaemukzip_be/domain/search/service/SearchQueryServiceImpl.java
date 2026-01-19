package com.mohaemukzip.mohaemukzip_be.domain.search.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.SummaryRepository;
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
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SearchQueryServiceImpl implements SearchQueryService {

    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    private final SummaryRepository summaryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public SearchQueryServiceImpl(
            IngredientRepository ingredientRepository,
            RecipeRepository recipeRepository,
            SummaryRepository summaryRepository,
            @Qualifier("redisCacheTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
        this.summaryRepository = summaryRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SearchResponseDTO search(String keyword) {
        // 빈 키워드 방어 로직
        if (keyword == null || keyword.isBlank()) {
            return SearchConverter.toEmptySearchResponseDTO();
        }

        String cacheKey = "search::" + keyword;

        // 1. Redis 캐시 조회
        try {
            SearchResponseDTO cachedData = (SearchResponseDTO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return cachedData;
            }
        } catch (Exception e) {
            // Redis 조회 실패 시 DB 조회 진행
        }

        // 2. DB 조회
        List<SearchResultDTO> results = new ArrayList<>();

        // 재료 검색
        results.addAll(searchIngredients(keyword));

        // 메뉴(레시피) 검색
        results.addAll(searchRecipes(keyword));

        SearchResponseDTO response = SearchConverter.toSearchResponseDTO(results);

        // 3. Redis 캐시 저장 (TTL 30분)
        try {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(30));
        } catch (Exception e) {
            // Redis 저장 실패 시 무시
        }

        return response;
    }

    // 재료 검색 로직
    private List<SearchResultDTO> searchIngredients(String keyword) {
        List<Ingredient> ingredients = ingredientRepository.findByNameContaining(keyword);
        return ingredients.stream()
                .map(SearchConverter::toSearchResultDTO)
                .collect(Collectors.toList());
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

        // 요약 레시피 제목 검색
        List<Summary> summaries = summaryRepository.findByTitleContaining(keyword);
        for (Summary summary : summaries) {
            Recipe recipe = summary.getRecipe();
            if (recipe != null) {
                // 이미 존재하는 레시피는 건너뜀 (레시피 제목 검색 우선)
                recipeMap.putIfAbsent(recipe.getId(), SearchConverter.toSearchResultDTO(recipe));
            }
        }

        return new ArrayList<>(recipeMap.values());
    }
}
