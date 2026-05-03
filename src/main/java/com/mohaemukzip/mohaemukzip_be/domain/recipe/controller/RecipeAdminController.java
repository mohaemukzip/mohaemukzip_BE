package com.mohaemukzip.mohaemukzip_be.domain.recipe.controller;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.RecipeEmbeddingService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 전용 레시피 어드민 컨트롤러.
 *
 * [역할]
 * - 일반 사용자용 RecipeController와 분리하여 관리자 전용 기능을 모아두는 컨트롤러입니다.
 * - GET /api/admin/recipes/embedding 호출 시 임베딩 배치 작업을 수동으로 트리거합니다.
 *
 * [보안 주의사항]
 * - 이 엔드포인트는 관리자만 접근해야 합니다.
 * - Spring Security 설정에서 /api/admin/** 경로에 ROLE_ADMIN 권한 제한을 추가하는 것을 권장합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/recipes")
@Tag(name = "Recipe Admin", description = "레시피 관리자 전용 API")
public class RecipeAdminController {

    private final RecipeEmbeddingService recipeEmbeddingService;

    /**
     * 임베딩 배치 작업 수동 실행 API.
     *
     * embedding 컬럼이 null인 레시피들을 찾아 FastAPI 서버로부터 임베딩 벡터를 받아 저장합니다.
     * 완료 시 처리 결과 요약 문자열(성공/실패 건수)을 반환합니다.
     *
     * @return 처리 결과 요약 (예: "전체 100건 중 성공: 98건, 실패: 2건")
     */
    @GetMapping("/embedding")
    @Operation(
            summary = "레시피 임베딩 배치 실행 API",
            description = "DB에서 embedding이 null인 레시피를 모두 찾아 FastAPI 서버로 임베딩 요청을 보내고 결과를 저장합니다."
    )
    public ApiResponse<String> generateEmbeddings() {
        String result = recipeEmbeddingService.generateMissingEmbeddings();
        return ApiResponse.onSuccess(result);
    }
}
