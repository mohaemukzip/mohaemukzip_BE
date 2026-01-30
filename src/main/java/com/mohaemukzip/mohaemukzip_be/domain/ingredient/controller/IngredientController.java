package com.mohaemukzip.mohaemukzip_be.domain.ingredient.controller;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.IngredientCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.IngredientQueryService;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.RecentSearchService;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ingredients")
@Tag(name = "Ingredient", description = "재료 관련 API")
@Validated
public class IngredientController {

    private final RecentSearchService recentSearchService;
    private final IngredientQueryService ingredientQueryService;
    private final IngredientCommandService ingredientCommandService;

    @Operation(summary = "즐겨찾기 여부 포함 재료 검색")
    @GetMapping
    public ApiResponse<IngredientResponseDTO.IngredientPageResponse> searchIngredients(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false) Category category,
            @RequestParam(name = "page", defaultValue = "0") int page
    ) {

        Long memberId = (customUserDetails != null)
                ? customUserDetails.getMember().getId()
                : null;

        if (memberId != null && query != null && !query.isBlank()) {
            recentSearchService.saveRecentSearch(memberId, query);
        }

        Page<IngredientResponseDTO.Detail> pageResponse = ingredientQueryService.getIngredients(memberId, query, category, page);
        IngredientResponseDTO.IngredientPageResponse response = IngredientResponseDTO.IngredientPageResponse.from(pageResponse);
        return ApiResponse.onSuccess(response);
    }



    @Operation(summary = "즐겨찾기 재료 목록 조회")
    @GetMapping("/favorites")
    public ApiResponse<List<IngredientResponseDTO.FavoriteDetail>> getFavoriteList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }
        Long memberId = customUserDetails.getMember().getId();

        List<IngredientResponseDTO.FavoriteDetail> response = ingredientQueryService.getFavoriteList(memberId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "즐겨찾기 재료 등록")
    @PostMapping("/{ingredientId}/favorites")
    public ApiResponse<IngredientResponseDTO.AddFavorite> addFavorite(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long ingredientId
    ) {

        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }
        Long memberId = customUserDetails.getMember().getId();

        IngredientResponseDTO.AddFavorite result =
                ingredientCommandService.addFavorite(memberId, ingredientId);

        return ApiResponse.onSuccess(result);
    }

    @Operation(summary = "즐겨찾기 재료 삭제")
    @DeleteMapping("/favorites/{memberFavoriteId}")
    public ApiResponse<IngredientResponseDTO.DeleteFavorite> deleteFavorite(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long memberFavoriteId
    ) {
        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }
        Long memberId = customUserDetails.getMember().getId();

        IngredientResponseDTO.DeleteFavorite result =
                ingredientCommandService.deleteFavorite(memberId, memberFavoriteId);

        return ApiResponse.onSuccess(result);


    }

    @Operation(summary = "최근 재료 검색어 조회")
    @GetMapping("/recent-searches")
    public ApiResponse<List<String>> getRecentSearches(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }

        Long memberId = customUserDetails.getMember().getId();
        List<String> recentSearches = recentSearchService.getRecentSearches(memberId);

        return ApiResponse.onSuccess(recentSearches);
    }

    @Operation(summary = "최근 재료 검색어 삭제")
    @DeleteMapping("/recent-searches")
    public ApiResponse<String> deleteRecentSearch(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam String keyword) {

        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }

        Long memberId = customUserDetails.getMember().getId();
        recentSearchService.deleteRecentSearch(memberId, keyword);

        return ApiResponse.onSuccess("최근 검색어가 삭제되었습니다.");
    }

    @Operation(summary = "재료 추가 요청")
    @PostMapping("/requests")
    public ApiResponse<String> ingredientRequest(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestBody IngredientRequestDTO.IngredientReq request
    ) {
        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }
        Long memberId = customUserDetails.getMember().getId();

        ingredientCommandService.createIngredientRequest(memberId, request);

        return ApiResponse.onSuccess("소중한 의견 감사합니다 *.* ");
    }

}


