package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRecipeService {

    private final RecipeCommandService recipeCommandService;
    private final RecipeEmbeddingService recipeEmbeddingService;

    @Async
    public void processBulkRecipesAsync(List<String> videoIds) {
        log.info("[관리자] 대량 레시피 비동기 등록 시작 - 총 {}건", videoIds.size());
        
        int successCount = 0;
        int failCount = 0;

        for (String videoId : videoIds) {
            try {
                // 1. 레시피 기본 정보 크롤링 및 저장
                Long recipeId = recipeCommandService.saveRecipeByVideoId(videoId);
                
                // 2. 요약 및 스텝 생성
                recipeCommandService.createSummary(recipeId);
                
                successCount++;
                log.info("[관리자] 레시피 및 요약 저장 성공 - videoId: {}", videoId);
            } catch (Exception e) {
                failCount++;
                log.error("[관리자] 레시피 처리 실패 - videoId: {}, 사유: {}", videoId, e.getMessage());
            }
        }

        // 3. 임베딩이 없는 레시피 일괄 처리 (방금 추가된 레시피 포함)
        log.info("[관리자] 일괄 임베딩 생성 시작");
        try {
            String embeddingResult = recipeEmbeddingService.generateMissingEmbeddings();
            log.info("[관리자] 일괄 임베딩 생성 결과: {}", embeddingResult);
        } catch (Exception e) {
            log.error("[관리자] 일괄 임베딩 생성 중 오류 발생: {}", e.getMessage());
        }

        log.info("[관리자] 대량 레시피 비동기 등록 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
    }
}
