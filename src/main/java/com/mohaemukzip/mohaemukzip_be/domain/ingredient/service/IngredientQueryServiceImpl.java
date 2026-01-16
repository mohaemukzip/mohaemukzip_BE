package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientQueryServiceImpl implements IngredientQueryService {

    private final IngredientRepository ingredientRepository;
    private final MemberIngredientRepository memberIngredientRepository;

    @Override
    @Transactional(readOnly = true)
    // DB 재료 조회
    public List<IngredientResponseDTO.Detail> getIngredients(String keyword, Category category) {

        List<Ingredient> ingredients;

        // 1. 키워드 + 카테고리가 있는 경우 조회
        if (keyword != null && !keyword.isBlank() && category != null) {
            ingredients = ingredientRepository.findByNameContainingAndCategory(keyword, category);
        }
        // 2. 키워드만 있는 경우
        else if (keyword != null && !keyword.isBlank()) {
            ingredients = ingredientRepository.findByNameContaining(keyword);
        }
        // 3. 카테고리만 있는 경우
        else if (category != null) {
            ingredients = ingredientRepository.findByCategory(category);
        }
        // 4. 둘 다 없는 경우 (전체 출력)
        else {
            ingredients = ingredientRepository.findAll();
        }

        return ingredients.stream()
                .map(IngredientResponseDTO.Detail::from)
                .collect(Collectors.toList());
    }

    // 냉장고 재료 조회
    @Override
    @Transactional(readOnly = true)
    public IngredientResponseDTO.FridgeIngredientList getMyFridgeIngredients(Long memberId) {

        List<MemberIngredient> memberIngredients = memberIngredientRepository.findAllByMemberId(memberId);

        List<IngredientResponseDTO.FridgeIngredient> dtoList = memberIngredients.stream()
                .map(IngredientResponseDTO.FridgeIngredient::from)
                .collect(Collectors.toList());

        return IngredientResponseDTO.FridgeIngredientList.builder()
                .fridgeList(dtoList)
                .build();
    }

}