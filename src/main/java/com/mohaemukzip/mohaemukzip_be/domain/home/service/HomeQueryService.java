package com.mohaemukzip.mohaemukzip_be.domain.home.service;

import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeResponseDTO;

public interface HomeQueryService {
    HomeResponseDTO getHome(Long memberId);
}
