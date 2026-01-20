package com.mohaemukzip.mohaemukzip_be.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResultDTO {
    private SearchType type;
    private Long id;
    private String name;  // INGREDIENT
    private String title; // RECIPE
}
