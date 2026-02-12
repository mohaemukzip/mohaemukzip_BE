package com.mohaemukzip.mohaemukzip_be.domain.home.service.query;

import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeCalendarResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeStatsResponseDTO;

public interface HomeQueryService {
    HomeResponseDTO getHome(Long memberId);
    HomeStatsResponseDTO getStats(Long memberId);
    HomeCalendarResponseDTO getCalendar(Long memberId, int year, int month);
}
