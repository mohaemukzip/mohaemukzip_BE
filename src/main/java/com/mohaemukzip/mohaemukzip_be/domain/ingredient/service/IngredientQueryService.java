package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import org.springframework.data.domain.Page;
import java.util.List;

public interface IngredientQueryService {

    // 즐겨찾기 여부 포함된 재료 목록 반환
    Page<IngredientResponseDTO.Detail> getIngredients(Long memberId, String keyword, Category category, Integer page);

    // 냉장고 조회
    IngredientResponseDTO.FridgeIngredientList getMyFridgeIngredients(Long memberId);

    //즐겨찾기 조회
    List<IngredientResponseDTO.Detail> getFavoriteList(Long memberId);

    //관리자용 재료 요청 목록 조회
    List<IngredientResponseDTO.AdminRequestList> getIngredientRequestList();

    // 재료 소비기한 추천 조회
    IngredientResponseDTO.RecommendedExpirationDate getRecommendedExpirationDate(
            Long ingredientId
    );


}
