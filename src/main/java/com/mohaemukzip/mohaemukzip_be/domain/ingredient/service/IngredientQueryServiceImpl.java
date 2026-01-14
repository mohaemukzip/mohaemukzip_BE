package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngredientQueryServiceImpl implements IngredientQueryService {

    private final IngredientRepository ingredientRepository;

    @Override
    @Transactional(readOnly = true)
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
}
