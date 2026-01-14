package com.mohaemukzip.mohaemukzip_be.domain.ingredient.controller;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.IngredientCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.IngredientQueryService;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ingredients")
@Tag(name = "Ingredient")
@Validated
public class IngredientController {

    private final IngredientQueryService ingredientQueryService;
    private final IngredientCommandService ingredientCommandService;

    // 재료 검색
    @GetMapping
    public ApiResponse<List<IngredientResponseDTO.Detail>> searchIngredients(
      @RequestParam(name = "query", required = false) String query,
      @RequestParam(name = "category", required = false) Category category
    ) {

        List<IngredientResponseDTO.Detail> response = ingredientQueryService.getIngredients(query,category);

        return ApiResponse.onSuccess(response);
    }

    // 냉장고 재료 추가
    @PostMapping("/me")
    public ApiResponse<IngredientResponseDTO.AddFridgeResult> addFridgeIngredient(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody IngredientRequestDTO.AddFridge request
    ) {
        Member member = customUserDetails.getMember();

        IngredientResponseDTO.AddFridgeResult result = ingredientCommandService.addFridgeIngredient(member.getId(), request);

        return ApiResponse.onSuccess(result);
    }

}


