package com.mohaemukzip.mohaemukzip_be.domain.search.controller;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.RecipeQueryService;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.search.service.SearchQueryService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
@Tag(name = "Search", description = "통합 검색 API")
@Validated
public class SearchController {

    private final SearchQueryService searchQueryService;
    private final RecipeQueryService recipeQueryService;


    @GetMapping
    @Operation(summary = "통합 검색 API", description = "재료 및 레시피(메뉴)를 통합 검색합니다.")
    public ApiResponse<SearchResponseDTO> search(
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") @Size(min = 1, max = 100, message = "검색어는 1자 이상 100자 이하로 입력해주세요.") String keyword) {
        return ApiResponse.onSuccess(searchQueryService.search(keyword));
    }

    @GetMapping("/recipes")
    @Operation(summary = "세부 카테고리별 레시피 조회 API", description = "특정 세부 카테고리(categoryId)에 속하는 레시피 목록을 조회합니다.")
    @Parameters({
            @Parameter(name = "categoryId", description = "세부 카테고리 ID", required = true),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)")
    })
    public ApiResponse<RecipeResponseDTO.RecipePreviewListDTO> getRecipes(
            @RequestParam(name = "categoryId") @Positive Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") @PositiveOrZero Integer page,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Member member = (userDetails != null) ? userDetails.getMember() : null;
        return ApiResponse.onSuccess(recipeQueryService.getRecipesByCategoryId(categoryId, page, member));
    }
}
