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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
@Tag(name = "Search", description = "통합 검색 및 카테고리 조회 API")
@Validated
public class SearchController {

    private final SearchQueryService searchQueryService;
    private final RecipeQueryService recipeQueryService;

    @GetMapping
    @Operation(summary = "통합 검색 API", description = "레시피 제목을 검색합니다. (페이징 지원)")
    public ApiResponse<SearchResponseDTO> search(
            @Parameter(description = "검색어 (1자 이상)", required = true)
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") @Size(min = 1, max = 100, message = "검색어는 1자 이상 100자 이하로 입력해주세요.") String keyword,
            
            @Parameter(description = "페이지 번호 (0부터 시작, 기본값 0)")
            @RequestParam(name = "page", defaultValue = "0") @PositiveOrZero Integer page
    ) {
        return ApiResponse.onSuccess(searchQueryService.search(keyword, page));
    }

    @GetMapping("/recipes")
    @Operation(summary = "세부 카테고리별 레시피 조회 API", description = "특정 세부 카테고리(categoryId)에 속하는 레시피 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
    })
    @Parameters({
            @Parameter(name = "categoryId", description = "세부 카테고리 ID", required = true),
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)")
    })
    public ApiResponse<RecipeResponseDTO.RecipePreviewListDTO> getRecipes(
            @RequestParam(name = "categoryId") @Positive Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") @PositiveOrZero Integer page,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = (userDetails != null) ? userDetails.getMember().getId() : null;
        return ApiResponse.onSuccess(recipeQueryService.getRecipesByCategoryId(categoryId, page, memberId));
    }
}
