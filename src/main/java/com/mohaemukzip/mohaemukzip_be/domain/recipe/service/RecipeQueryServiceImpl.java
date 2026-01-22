package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeCategoryRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeQueryServiceImpl implements RecipeQueryService {

    private final RecipeCategoryRepository recipeCategoryRepository;
    private static final int PAGE_SIZE = 10;

    @Override
    public RecipeResponseDTO.RecipePreviewListDTO getRecipesByCategoryId(Long categoryId, Integer page) {
        Page<Recipe> recipePage = recipeCategoryRepository.findRecipesByCategoryId(categoryId, PageRequest.of(page, PAGE_SIZE));

        if (recipePage.isEmpty()) {
            throw new BusinessException(ErrorStatus.CATEGORY_NOT_FOUND);
        }

        return RecipeConverter.toRecipePreviewListDTO(recipePage);
    }
}
