package com.mohaemukzip.mohaemukzip_be.domain.recipe.controller;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.RecipeQueryService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search/recipes")
@Tag(name = "Recipe", description = "레시피 관련 API")
@Validated
public class RecipeController {

    private final RecipeQueryService recipeQueryService;

    @GetMapping
    @Operation(summary = "세부 카테고리별 레시피 조회 API", description = "특정 세부 카테고리(categoryId)에 속하는 레시피 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
    })
    @Parameters({
            @Parameter(name = "categoryId", description = "세부 카테고리 ID", required = true),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)")
    })
    public ApiResponse<RecipeResponseDTO.RecipePreviewListDTO> getRecipes(
            @RequestParam(name = "categoryId") Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") Integer page
    ) {
        return ApiResponse.onSuccess(recipeQueryService.getRecipesByCategoryId(categoryId, page));
    }
}
