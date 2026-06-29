package com.mohaemukzip.mohaemukzip_be.domain.recipe.controller;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.RecipeCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.RecipeEmbeddingService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/recipes")
@Tag(name = "Admin Recipe", description = "관리자 전용 레시피 API")
public class AdminRecipeController {

    private final RecipeCommandService recipeCommandService;
    private final RecipeEmbeddingService recipeEmbeddingService;

    public record BulkRecipeRequest(List<String> videoIds) {}

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "레시피 대량 등록 (임베딩 포함)", description = "관리자 전용. 다수의 videoId를 입력받아 레시피를 순차 저장하고 임베딩을 일괄 생성합니다.")
    public ApiResponse<Map<String, Object>> createRecipesInBulk(@RequestBody BulkRecipeRequest request) {
        log.info("[관리자] 대량 레시피 등록 시작 - 요청 건수: {}", request.videoIds().size());

        int successCount = 0;
        int failCount = 0;

        // 1. 순차적으로 레시피 크롤링 및 기본 정보 저장
        for (String videoId : request.videoIds()) {
            try {
                recipeCommandService.saveRecipeByVideoId(videoId);
                successCount++;
                log.info("[관리자] 레시피 저장 성공 - videoId: {}", videoId);
            } catch (Exception e) {
                failCount++;
                log.error("[관리자] 레시피 저장 실패 - videoId: {}, 사유: {}", videoId, e.getMessage());
            }
        }

        // 2. 저장된 레시피 중 임베딩이 없는 레시피 일괄 임베딩 처리
        log.info("[관리자] 일괄 임베딩 생성 시작");
        String embeddingResult = recipeEmbeddingService.generateMissingEmbeddings();

        return ApiResponse.onSuccess(Map.of(
                "message", "대량 등록 작업 완료",
                "recipe_save_success", successCount,
                "recipe_save_fail", failCount,
                "embedding_result", embeddingResult
        ));
    }
}
