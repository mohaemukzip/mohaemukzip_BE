package com.mohaemukzip.mohaemukzip_be.domain.home.controller;

import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeCalendarResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeStatsResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.home.service.HomeQueryService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 화면 API")
@Validated
public class HomeController {

    private final HomeQueryService homeQueryService;

    @Operation(summary = "홈 화면 조회")
    @GetMapping
    public ApiResponse<HomeResponseDTO> getHome(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        HomeResponseDTO response = homeQueryService.getHome(memberId);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "홈 통계 조회")
    @GetMapping("/stats")
    public ApiResponse<HomeStatsResponseDTO> getStats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        HomeStatsResponseDTO response = homeQueryService.getStats(memberId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "홈 캘린더 조회")
    @GetMapping("/stats/calendar")
    public ApiResponse<HomeCalendarResponseDTO> getCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @Positive int year,
            @RequestParam @Min(1) @Max(12) int month) {
        Long memberId = userDetails.getMember().getId();
        HomeCalendarResponseDTO response = homeQueryService.getCalendar(memberId, year, month);
        return ApiResponse.onSuccess(response);
    }
}
