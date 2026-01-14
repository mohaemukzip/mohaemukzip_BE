package com.mohaemukzip.mohaemukzip_be.domain.ingredient.controller;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.IngredientQueryService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ingredients")
@Tag(name = "Ingredient")
@Validated
public class IngredientController {

    private final IngredientQueryService ingredientQueryService;

    @GetMapping
    public ApiResponse<List<IngredientResponseDTO>> searchIngredients(
      @RequestParam(name = "query", required = false) String query,
      @RequestParam(name = "category", required = false) Category category
    ) {
        List<IngredientResponseDTO> response = ingredientQueryService.getIngredients(query,category);

        return ApiResponse.onSuccess(response);
    }
}


