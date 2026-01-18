package com.mohaemukzip.mohaemukzip_be.domain.search.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponseDTO {
    private List<SearchResultDTO> results;
}
