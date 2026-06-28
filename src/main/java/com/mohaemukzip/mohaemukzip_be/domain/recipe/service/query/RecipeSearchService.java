package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.query;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeSearchResponseDto;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.global.client.EmbeddingClient;
import com.mohaemukzip.mohaemukzip_be.global.util.VectorMathUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 레시피 벡터 검색 서비스.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecipeSearchService {

    private final RecipeRepository recipeRepository;
    private final EmbeddingClient embeddingClient;

    /**
     * 사용자의 질문(query)을 기반으로 가장 유사한 레시피 상위 3개를 검색합니다.
     *
     * @param query 사용자의 자연어 질문 (예: "매콤한 떡볶이 레시피")
     * @return 유사도 상위 3개 레시피 리스트
     */
    public List<RecipeSearchResponseDto> searchTop3ByVector(String query) {
        log.info("[벡터 검색] 시작 - 쿼리: {}", query);

        // 1. 사용자 질문을 임베딩 벡터로 변환
        List<Double> queryVector = embeddingClient.getEmbedding(query);

        // 2. DB에서 임베딩이 존재하는 모든 레시피 조회
        List<Recipe> recipesWithEmbedding = recipeRepository.findByEmbeddingIsNotNull();

        // 3. 코사인 유사도 계산 및 상위 3개 정렬 (유사도 0.75 이상만)
        return recipesWithEmbedding.stream()
                .filter(recipe -> recipe.getEmbedding() != null)
                .filter(recipe -> !recipe.getEmbedding().isEmpty())
                .filter(recipe -> recipe.getEmbedding().size() == queryVector.size())
                .map(recipe -> RecipeSearchResponseDto.builder()
                        .id(recipe.getId())
                        .title(recipe.getTitle())
                        .similarity(VectorMathUtil.cosineSimilarity(queryVector, recipe.getEmbedding()))
                        .build())
                .filter(dto -> Double.isFinite(dto.getSimilarity()))
                .filter(dto -> dto.getSimilarity() >= 0.75) // [수정] 고차원 벡터 특성을 고려하여 임계값을 0.75로 대폭 상향
                .sorted(Comparator.comparing(RecipeSearchResponseDto::getSimilarity).reversed()) // 유사도 높은 순
                .limit(3) // 상위 3개
                .collect(Collectors.toList());
    }
}
