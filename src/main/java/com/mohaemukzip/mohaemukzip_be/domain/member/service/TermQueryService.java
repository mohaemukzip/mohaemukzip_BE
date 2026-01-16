package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.TermResponseDTO;

public interface TermQueryService {
    TermResponseDTO.TermListResponse getTerms();
}
