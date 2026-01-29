package com.mohaemukzip.mohaemukzip_be.domain.search.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeProjection;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResultDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public class SearchConverter {

    public static SearchResultDTO toSearchResultDTO(Recipe recipe) {
        return SearchResultDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .build();
    }

    public static SearchResultDTO toSearchResultDTO(RecipeProjection projection) {
        return SearchResultDTO.builder()
                .id(projection.getId())
                .title(projection.getTitle())
                .build();
    }

    public static SearchResponseDTO toSearchResponseDTO(List<SearchResultDTO> results) {
        return SearchResponseDTO.builder()
                .results(results)
                .build();
    }

    public static SearchResponseDTO toSearchResponseDTO(Page<RecipeProjection> page) {
        List<SearchResultDTO> results = page.getContent().stream()
                .map(SearchConverter::toSearchResultDTO)
                .collect(Collectors.toList());

        return SearchResponseDTO.builder()
                .results(results)
                .totalPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
    
    public static SearchResponseDTO toEmptySearchResponseDTO() {
        return SearchResponseDTO.builder()
                .results(List.of())
                .totalPage(0)
                .totalElements(0L)
                .isFirst(true)
                .isLast(true)
                .build();
    }
}
