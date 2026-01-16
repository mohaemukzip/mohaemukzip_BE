package com.mohaemukzip.mohaemukzip_be.domain.member.controller;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.service.AuthCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.member.service.AuthQueryService;
import com.mohaemukzip.mohaemukzip_be.global.jwt.JwtProvider;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth")
@Validated
public class AuthController {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입 (일반)")
    @PostMapping("/signup")
    public ApiResponse<AuthResponseDTO.GetUserDTO> signup(@Valid @RequestBody AuthRequestDTO.SignUpRequest request) {

        AuthResponseDTO.GetUserDTO response = authCommandService.signup(request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "로그인 (일반)")
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO.GetUserDTO> login(@Valid @RequestBody AuthRequestDTO.LoginRequest request) {

        AuthResponseDTO.GetUserDTO response = authCommandService.login(request);

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ApiResponse<AuthResponseDTO.TokenResponse> reissueToken(
            @RequestHeader("Authorization") String expiredAccessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        String oldAt = expiredAccessToken.substring(7); // "Bearer " 제거
        AuthResponseDTO.TokenResponse response = authCommandService.reissueToken(refreshToken);
        return ApiResponse.onSuccess(response);
    }
//
//    @Operation(summary = "로그아웃")
//    @PostMapping("/logout")
//    public ApiResponse<Void> logout(
//            @RequestHeader("Authorization") String accessToken) {
//        String token = accessToken.substring(7);
//        authCommandService.logout(token);
//        return ApiResponse.onSuccess(null);
//    }

    @Operation(summary = "아이디 중복 확인")
    @PostMapping("/check-loginid")
    public ApiResponse<AuthResponseDTO.CheckLoginIdResponse> checkLoginId(
            @Valid @RequestBody AuthRequestDTO.CheckLoginIdRequest request) {

        boolean isDuplicate = authQueryService.checkLoginIdDuplicate(request.loginId());

        AuthResponseDTO.CheckLoginIdResponse response = isDuplicate
                ? AuthResponseDTO.CheckLoginIdResponse.notAvailable()
                : AuthResponseDTO.CheckLoginIdResponse.available();

        return ApiResponse.onSuccess(response);
    }
}
