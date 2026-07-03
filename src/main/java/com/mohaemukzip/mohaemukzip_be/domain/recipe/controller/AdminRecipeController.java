package com.mohaemukzip.mohaemukzip_be.domain.recipe.controller;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.RecipeCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.RecipeEmbeddingService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;

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
    private final com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.AdminRecipeService adminRecipeService;

    public record BulkRecipeRequest(Long dishId, List<String> videoIds) {}

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "레시피 대량 등록 (임베딩 포함)", description = "관리자 전용. 다수의 videoId를 입력받아 레시피를 순차 저장하고 임베딩을 일괄 생성합니다.")
    public ApiResponse<String> createRecipesInBulk(@RequestBody BulkRecipeRequest request) {
        log.info("[관리자] 대량 레시피 비동기 등록 요청 - 요리ID: {}, 건수: {}", request.dishId(), request.videoIds().size());

        // 비동기 서비스 호출 (레시피 크롤링 -> 요약 -> 임베딩)
        adminRecipeService.processBulkRecipesAsync(request.dishId(), request.videoIds());

        return ApiResponse.onSuccess("레시피 대량 등록 작업이 백그라운드에서 시작되었습니다.");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "레시피 저장 API (단건)", description = "관리자 전용. 특정 video_id를 가진 유튜브 영상에 관한 레시피를 저장합니다.")
    public ApiResponse<RecipeResponseDTO.RecipeCreateResponse> createRecipe(
            @RequestBody RecipeRequestDTO.RecipeCreateRequest request
    ) {
        Long recipeId = recipeCommandService.saveRecipeByVideoId(request.getDishId(), request.getVideoId());
        return ApiResponse.onSuccess(new RecipeResponseDTO.RecipeCreateResponse(recipeId));
    }

    @PostMapping("/{recipeId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "요약 레시피 생성 API (단건)", description = "관리자 전용. 특정 RecipeId에 속하는 레시피의 조리법을 요약해서 저장합니다.")
    public ApiResponse<RecipeResponseDTO.SummaryCreateResponse> createSummary(
            @PathVariable Long recipeId
    ) {
        var result = recipeCommandService.createSummary(recipeId);
        return ApiResponse.onSuccess(
                new RecipeResponseDTO.SummaryCreateResponse(result.summaryExists(), result.stepCount())
        );
    }

    @PostMapping("/embedding")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "레시피 임베딩 배치 실행 API",
            description = "관리자 전용. DB에서 embedding이 null인 레시피를 모두 찾아 OpenAPI 서버로 임베딩 요청을 보내고 결과를 저장합니다."
    )
    public ApiResponse<String> generateEmbeddings() {
        String result = recipeEmbeddingService.generateMissingEmbeddings();
        return ApiResponse.onSuccess(result);
    }
}
