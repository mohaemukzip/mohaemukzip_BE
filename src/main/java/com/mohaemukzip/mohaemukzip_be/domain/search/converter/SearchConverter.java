package com.mohaemukzip.mohaemukzip_be.domain.search.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResultDTO;

import java.util.List;

public class SearchConverter {

    public static SearchResultDTO toSearchResultDTO(Recipe recipe) {
        return SearchResultDTO.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .build();
    }

    public static SearchResponseDTO toSearchResponseDTO(List<SearchResultDTO> results) {
        return SearchResponseDTO.builder()
                .results(results)
                .build();
    }
    
    public static SearchResponseDTO toEmptySearchResponseDTO() {
        return SearchResponseDTO.builder()
                .results(List.of())
                .build();
    }
}
