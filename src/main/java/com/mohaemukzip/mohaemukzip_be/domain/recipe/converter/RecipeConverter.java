package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class RecipeConverter {

    public static RecipeResponseDTO.RecipePreviewDTO toRecipePreviewDTO(Recipe recipe) {
        return RecipeResponseDTO.RecipePreviewDTO.builder()
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .imageUrl(recipe.getImageUrl())
                .cookingTime(recipe.getCookingTime())
                .level(recipe.getLevel())
                .ratingCount(recipe.getRatingCount())
                .build();
    }

    public static RecipeResponseDTO.RecipePreviewListDTO toRecipePreviewListDTO(Page<Recipe> recipePage) {
        List<RecipeResponseDTO.RecipePreviewDTO> recipePreviewDTOList = recipePage.stream()
                .map(RecipeConverter::toRecipePreviewDTO)
                .collect(Collectors.toList());

        return RecipeResponseDTO.RecipePreviewListDTO.builder()
                .isLast(recipePage.isLast())
                .isFirst(recipePage.isFirst())
                .totalPage(recipePage.getTotalPages())
                .totalElements(recipePage.getTotalElements())
                .listSize(recipePreviewDTOList.size())
                .recipeList(recipePreviewDTOList)
                .build();
    }
}
