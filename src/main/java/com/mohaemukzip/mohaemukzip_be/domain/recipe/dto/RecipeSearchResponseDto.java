package com.mohaemukzip.mohaemukzip_be.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 벡터 검색 결과를 담는 DTO.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeSearchResponseDto {

    private Long id;
    private String title;
    private Double similarity;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeSearchListResponseDto {
        private List<RecipeSearchResponseDto> searchResults;
    }
}
