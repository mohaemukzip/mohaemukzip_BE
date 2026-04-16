package com.mohaemukzip.mohaemukzip_be.domain.ingredient.controller;


import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.IngredientQueryService;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.enums.Role;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin Request Ingredients", description = "[관리자용] 재료 요청 목록")
@Validated
public class AdminController {


    private final IngredientQueryService ingredientQueryService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ingredients/requests")
    @Operation(summary = "관리자용 재료 요청 목록 조회")
    public ApiResponse<List<IngredientResponseDTO.AdminRequestList>> getIngredientRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<IngredientResponseDTO.AdminRequestList> result =
                ingredientQueryService.getIngredientRequestList();

        return ApiResponse.onSuccess(result);
    }
}
