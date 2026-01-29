package com.mohaemukzip.mohaemukzip_be.domain.ingredient.controller;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.IngredientCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.IngredientQueryService;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final IngredientQueryService ingredientQueryService;
    private final IngredientCommandService ingredientCommandService;

    @Operation(summary = "즐겨찾기 여부 포함 재료 검색")
    @GetMapping
    public ApiResponse<List<IngredientResponseDTO.Detail>> searchIngredients(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "category", required = false) Category category
    ) {

        Long memberId = null;

        if (customUserDetails != null) {
            memberId = customUserDetails.getMember().getId();

            if (query != null && !query.isBlank()) {
                try {
                    ingredientCommandService.saveRecentSearch(memberId, query);
                } catch (Exception e) {
                    log.warn("최근 검색어 저장 실패 - MemberId: {}, Query: {}", memberId, query, e);
                }
            }
        }

        List<IngredientResponseDTO.Detail> response = ingredientQueryService.getIngredients(memberId, query, category);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "즐겨찾기 재료 목록 조회")
    @GetMapping("/favorites")
    public ApiResponse<IngredientResponseDTO.FavoriteList> getFavoriteList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }
        Long memberId = customUserDetails.getMember().getId();

        IngredientResponseDTO.FavoriteList response = ingredientQueryService.getFavoriteList(memberId);

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
    @DeleteMapping("/favorites/{favoriteId}")
    public ApiResponse<IngredientResponseDTO.DeleteFavorite> deleteFavorite(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable Long favoriteId
    ) {
        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }
        Long memberId = customUserDetails.getMember().getId();

        IngredientResponseDTO.DeleteFavorite result =
                ingredientCommandService.deleteFavorite(memberId, favoriteId);

        return ApiResponse.onSuccess(result);


    }

    @Operation(summary = "최근 재료 검색어 조회")
    @GetMapping("/recent-searches")
    public ApiResponse<IngredientResponseDTO.RecentSearchList> getRecentSearch(
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }
        Long memberId = customUserDetails.getMember().getId();

        IngredientResponseDTO.RecentSearchList result =
                ingredientQueryService.getRecentSearch(memberId);

        return ApiResponse.onSuccess(result);
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
  
    @Operation(summary = "최근 재료 검색어 삭제")
    @DeleteMapping("/recent-searches/{recentSearchId}")
    public ApiResponse<String> deleteRecentSearch(

            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable(name = "recentSearchId") Long recentSearchId
    ) {
        if (customUserDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }

        Long memberId = customUserDetails.getMember().getId();

        ingredientCommandService.deleteRecentSearch(memberId, recentSearchId);

        return ApiResponse.onSuccess("최근 검색어가 삭제되었습니다.");
    }
}


