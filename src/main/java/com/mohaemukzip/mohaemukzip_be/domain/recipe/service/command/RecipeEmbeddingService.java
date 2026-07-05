package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.global.client.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 레시피 임베딩 배치 처리 서비스.
 *
 * [동작 흐름]
 * 1. DB에서 모든 레시피 목록을 가져옵니다.
 * 2. embedding 컬럼이 null이거나 1536차원이 아닌(과거 1024차원) 레시피를 타겟으로 필터링합니다.
 * 3. 각 타겟 레시피에 대해 OpenAI 서버에 임베딩 요청을 보냅니다.
 * 4. 받아온 1536차원 벡터를 Recipe 엔티티에 업데이트하고 저장합니다.
 * 5. 오류가 발생한 레시피는 로그로 남기고 스킵합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeEmbeddingService {

    private final RecipeRepository recipeRepository;
    private final EmbeddingClient embeddingClient;

    private static final int EMBEDDING_DIMENSION = 768;

    /**
     * embedding이 없거나 차원이 다른(1024 등) 모든 레시피에 임베딩 벡터를 생성하고 덮어씁니다.
     *
     * @return 처리 결과 요약 문자열
     */
    public String generateMissingEmbeddings() {
        int page = 0;
        int size = 20; // 20건씩 처리
        int totalProcessed = 0;
        int successCount = 0;
        int failCount = 0;

        log.info("[임베딩 배치] 시작 - 배치 사이즈: {}", size);

        while (true) {
            org.springframework.data.domain.Page<Recipe> recipePage = recipeRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
            
            List<Recipe> targetRecipes = recipePage.stream()
                    .filter(recipe -> recipe.getEmbedding() == null || recipe.getEmbedding().size() != EMBEDDING_DIMENSION)
                    .toList();

            totalProcessed += targetRecipes.size();

            // 2. 각 레시피를 순회하며 임베딩 갱신
            for (Recipe recipe : targetRecipes) {
                try {
                    log.info("[임베딩 배치] 처리 중 - ID: {}, 제목: {}", recipe.getId(), recipe.getTitle());

                    // 레시피 제목으로 임베딩 벡터 요청 (Gemini API)
                    List<Double> embedding = embeddingClient.getEmbedding(recipe.getTitle());

                    // 임베딩 응답 검증 (차원 불일치 시 검색 품질 저하 방지)
                    if (embedding == null || embedding.size() != EMBEDDING_DIMENSION) {
                        int actualSize = (embedding == null) ? 0 : embedding.size();
                        throw new IllegalStateException(String.format(
                                "유효하지 않은 임베딩 응답 (기대: %d, 실제: %d)", EMBEDDING_DIMENSION, actualSize));
                    }

                    // 엔티티 업데이트
                    recipe.updateEmbedding(embedding);
                    recipeRepository.save(recipe);

                    successCount++;
                    log.info("[임베딩 배치] 완료 - ID: {}, 벡터 차원: {}", recipe.getId(), embedding.size());

                } catch (Exception e) {
                    failCount++;
                    log.error("[임베딩 배치] 실패 - ID: {}, 제목: {}, 오류: {}",
                            recipe.getId(), recipe.getTitle(), e.getMessage());
                }
            }

            if (!recipePage.hasNext()) {
                break;
            }
            page++;
        }

        String summary = String.format("[임베딩 배치] 완료 - 갱신 필요 레시피: %d건 중 성공: %d건, 실패: %d건",
                totalProcessed, successCount, failCount);
        log.info(summary);
        return summary;
    }
}
