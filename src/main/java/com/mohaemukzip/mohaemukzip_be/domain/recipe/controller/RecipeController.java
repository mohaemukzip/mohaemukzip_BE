package com.mohaemukzip.mohaemukzip_be.domain.recipe.controller;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeDetailResponseDTO;
import java.util.List;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeSearchResponseDto;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.RecipeCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.query.RecipeQueryService;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.query.RecipeSearchService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipes")
@Tag(name = "Recipe" , description = "레시피 관련 API")
@Validated
public class RecipeController {

    private final RecipeQueryService recipeQueryService;
    private final RecipeCommandService recipeCommandService;
    private final RecipeSearchService recipeSearchService;

    @PostMapping
    @Operation(summary = "레시피 저장 API", description = "특정 video_id를 가진 유튜브 영상에 관한 레시피를 저장합니다.")
    public ApiResponse<RecipeResponseDTO.RecipeCreateResponse> createRecipe(
            @RequestBody RecipeRequestDTO.RecipeCreateRequest request
    ) {
        Long recipeId = recipeCommandService.saveRecipeByVideoId(request.getVideoId());
        return ApiResponse.onSuccess(new RecipeResponseDTO.RecipeCreateResponse(recipeId));
    }


    @GetMapping("/{recipeId}")
    @Operation(summary = "세부 레시피 조회 API", description = "특정 videoId에 속하는 레시피에 관한 정보를 조회합니다.")
    public ApiResponse<RecipeDetailResponseDTO> getRecipeDetail(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                recipeQueryService.getRecipeDetail(recipeId, userDetails.getMember().getId())
        );
    }

    @PostMapping("{recipeId}/summary")
    @Operation(summary = "요약 레시피 생성 API", description = "특정 RecipeId에 속하는 레시피의 조리법을 요약해서 저장합니다.")
    public ApiResponse<RecipeResponseDTO.SummaryCreateResponse> createSummary(
            @PathVariable Long recipeId
    ) {
        var result = recipeCommandService.createSummary(recipeId);
        return ApiResponse.onSuccess(
                new RecipeResponseDTO.SummaryCreateResponse(result.summaryExists(), result.stepCount())
        );
    }
    
    @PostMapping("/{recipeId}/complete")
    @Operation(summary = "요리 완료 API", description = "특정 RecipeId에 속하는 레시피를 CookingRecord에 저장하고, 해당 레시피의 난이도를 갱신합니다.")
    public ApiResponse<RecipeResponseDTO.CookingRecordCreateResponseDTO> createCookingRecord(
            @PathVariable Long recipeId,
            @RequestParam int rating,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                recipeCommandService.createCookingRecord(
                        userDetails.getMember().getId(),
                        recipeId,
                        rating
                )
        );
    }

    @PostMapping("/{recipeId}/bookmark")
    @Operation(summary = "북마크 토글 API", description = "특정 레시피를 북마크에 저장하거나, 이미 저장된 경우 삭제합니다.")
    public ApiResponse<RecipeResponseDTO.BookmarkToggleResult> toggleBookmark(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                recipeCommandService.toggleBookmark(userDetails.getMember().getId(), recipeId)
        );
    }

    @GetMapping("/vector-search")
    @Operation(summary = "레시피 벡터 검색 API (RAG용)", description = "사용자의 자연어 질문을 기반으로 가장 유사한 레시피 3개를 벡터 검색으로 찾아옵니다.")
    public ApiResponse<List<RecipeSearchResponseDto>> searchRecipesByVector(
            @RequestParam
            @NotBlank
            @Size(max = 200)
            String query
    ) {
        return ApiResponse.onSuccess(recipeSearchService.searchTop3ByVector(query));
    }
}
