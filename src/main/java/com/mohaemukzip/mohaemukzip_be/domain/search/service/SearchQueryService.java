package com.mohaemukzip.mohaemukzip_be.domain.search.service;

import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResponseDTO;

public interface SearchQueryService {
    SearchResponseDTO search(String keyword);
}
