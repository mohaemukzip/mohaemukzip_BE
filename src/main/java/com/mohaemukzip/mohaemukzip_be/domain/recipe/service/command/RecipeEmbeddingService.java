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
 * 1. DB에서 embedding 컬럼이 null인 레시피 목록을 모두 가져옵니다.
 * 2. 각 레시피에 대해 EmbeddingClient를 통해 FastAPI 서버에 임베딩 요청을 보냅니다.
 * 3. 받아온 1024차원 벡터를 Recipe 엔티티에 업데이트하고 저장합니다.
 * 4. 오류가 발생한 레시피는 로그로 남기고 스킵합니다(다음 레시피 처리 계속).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeEmbeddingService {

    private final RecipeRepository recipeRepository;
    private final EmbeddingClient embeddingClient;

    private static final int EMBEDDING_DIMENSION = 1024;

    /**
     * embedding이 null인 모든 레시피에 임베딩 벡터를 생성하고 저장합니다.
     *
     * @return 처리 결과 요약 문자열 (예: "전체 100건 중 98건 성공, 2건 실패")
     */
    @Transactional
    public String generateMissingEmbeddings() {
        // 1. embedding이 null인 레시피 목록 조회
        List<Recipe> recipesWithoutEmbedding = recipeRepository.findByEmbeddingIsNull();

        int total = recipesWithoutEmbedding.size();
        int successCount = 0;
        int failCount = 0;

        log.info("[임베딩 배치] 시작 - 처리 대상 레시피: {}건", total);

        if (total == 0) {
            log.info("[임베딩 배치] 처리할 레시피가 없습니다. 모든 레시피에 임베딩이 존재합니다.");
            return "처리할 레시피가 없습니다. 모든 레시피에 임베딩이 이미 존재합니다.";
        }

        // 2. 각 레시피를 순회하며 임베딩 생성 및 저장
        for (Recipe recipe : recipesWithoutEmbedding) {
            try {
                log.info("[임베딩 배치] 처리 중 - ID: {}, 제목: {}", recipe.getId(), recipe.getTitle());

                // 레시피 제목으로 임베딩 벡터 요청 (FastAPI → HuggingFace 모델)
                List<Double> embedding = embeddingClient.getEmbedding(recipe.getTitle());

                // 임베딩 응답 검증 (차원 불일치 시 검색 품질 저하 방지)
                if (embedding == null || embedding.size() != EMBEDDING_DIMENSION) {
                    int actualSize = (embedding == null) ? 0 : embedding.size();
                    throw new IllegalStateException(String.format(
                            "유효하지 않은 임베딩 응답 (기대: %d, 실제: %d)", EMBEDDING_DIMENSION, actualSize));
                }

                // 엔티티 업데이트 (updateEmbedding 메서드가 직접 필드를 갱신)
                recipe.updateEmbedding(embedding);

                // @Transactional 덕분에 별도의 save() 호출 없이도 변경사항이 감지(Dirty Checking)되어 저장됨.
                // 하지만 명시성을 위해 save()를 호출해도 무방합니다.
                recipeRepository.save(recipe);

                successCount++;
                log.info("[임베딩 배치] 완료 - ID: {}, 벡터 차원: {}", recipe.getId(), embedding.size());

            } catch (Exception e) {
                // 하나의 레시피 실패 시 전체 배치를 중단하지 않고 계속 진행
                failCount++;
                log.error("[임베딩 배치] 실패 - ID: {}, 제목: {}, 오류: {}",
                        recipe.getId(), recipe.getTitle(), e.getMessage());
            }
        }

        String summary = String.format("[임베딩 배치] 완료 - 전체 %d건 중 성공: %d건, 실패: %d건",
                total, successCount, failCount);
        log.info(summary);
        return summary;
    }
}
