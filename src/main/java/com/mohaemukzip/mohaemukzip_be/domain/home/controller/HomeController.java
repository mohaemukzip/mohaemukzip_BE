package com.mohaemukzip.mohaemukzip_be.domain.home.controller;

import com.mohaemukzip.mohaemukzip_be.domain.home.dto.HomeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.home.service.HomeQueryService;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 화면 API")
@Validated
public class HomeController {

    private final HomeQueryService homequeryService;

    @Operation(summary = "홈 화면 조회")
    @GetMapping
    public ApiResponse<HomeResponseDTO> getHome(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        HomeResponseDTO response = homequeryService.getHome(memberId);

        return ApiResponse.onSuccess(response);
    }
}
