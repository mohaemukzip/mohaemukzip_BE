package com.mohaemukzip.mohaemukzip_be.domain.recipe.controller;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeDetailResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.RecipeCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.RecipeQueryService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipes")
@Tag(name = "Recipe" , description = "레시피 관련 API")
@Validated
@RequestMapping
public class RecipeController {

    private final RecipeQueryService recipeQueryService;
    private final RecipeCommandService recipeCommandService;

    @PostMapping
    @Operation(summary = "레시피 저장 API", description = "특정 video_id를 가진 유튜브 영상에 관한 레시피를 저장합니다.")
    public ApiResponse<RecipeResponseDTO.RecipeCreateResponse> createRecipe(
            @RequestBody RecipeResponseDTO.RecipeCreateRequest request
    ) {
        Long recipeId = recipeCommandService.saveRecipeByVideoId(request.getVideoId());
        return ApiResponse.onSuccess(new RecipeResponseDTO.RecipeCreateResponse(recipeId));
    }


    @GetMapping("/recipes/{recipeId}")
    @Operation(summary = "세부 레시피 조회 API", description = "특정 videoId에 속하는 레시피에 관한 정보를 조회합니다.")
    public ApiResponse<RecipeDetailResponseDTO> getRecipeDetail(
            @PathVariable Long recipeId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                recipeQueryService.getRecipeDetail(recipeId, userDetails.getMember().getId())
        );
    }

    @PostMapping("/recipes/{recipeId}/summary")
    @Operation(summary = "요약 레시피 생성 API", description = "특정 RecipeId에 속하는 레시피의 조리법을 요약해서 저장합니다.")
    public ApiResponse<SummaryCreateResponse> createSummary(
            @PathVariable Long recipeId
    ) {
        var result = recipeCommandService.createSummary(recipeId);
        return ApiResponse.onSuccess(
                new SummaryCreateResponse(result.summaryExists(), result.stepCount())
        );
    }

    public record SummaryCreateResponse(
            boolean summaryExists,
            int stepCount
    ) {}

}
