package com.mohaemukzip.mohaemukzip_be.domain.search.service;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeProjection;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
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

        String sanitizedKeyword = keyword.trim();
        String strippedKeyword = sanitizedKeyword.replace(" ", "");

        String normalizedKeyword = strippedKeyword.toLowerCase();
        String cacheKey = "search::recipe::" + normalizedKeyword + "::" + page;
        try {
            SearchResponseDTO cachedData = (SearchResponseDTO) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                return cachedData;
            }
        } catch (Exception e) {
            log.warn("Redis cache read failed. key={}", cacheKey, e);
        }

        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, Sort.by("id").ascending());
        Page<RecipeProjection> resultPage = recipeRepository.findProjectedByTitleContaining(strippedKeyword, pageable);

        SearchResponseDTO response = SearchConverter.toSearchResponseDTO(resultPage);

        try {
            redisTemplate.opsForValue().set(cacheKey, response, Duration.ofMinutes(30));
        } catch (Exception e) {
            log.warn("Redis cache read failed. key={}", cacheKey, e);
        }

        return response;
    }
}
