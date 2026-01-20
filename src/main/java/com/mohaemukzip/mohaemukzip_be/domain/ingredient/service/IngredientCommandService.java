package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;

public interface IngredientCommandService {

    //냉장고에 재료 등록
    IngredientResponseDTO.AddFridgeResult addFridgeIngredient(Long memberId, IngredientRequestDTO.AddFridge request);

    // 냉장고 재료 삭제
    IngredientResponseDTO.DeleteFridgeIngredient  deleteIngredient(Long memberIngredientId, Long memberId);

    //재료 즐겨찾기 등록
    IngredientResponseDTO.AddFavorite addFavorite(Long memberId, Long ingredientId);

    //재료 즐겨찾기 삭제
    IngredientResponseDTO.DeleteFavorite deleteFavorite(Long memberId, Long favoriteId);

    //최근 검색어 저장
    void saveRecentSearch(Long memberId, String keyword);
}
