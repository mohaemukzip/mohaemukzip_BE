package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthResponseDTO;

public interface AuthCommandService {
    AuthResponseDTO.GetUserDTO signup(AuthRequestDTO.SignUpRequest signUpRequest);
    AuthResponseDTO.GetUserDTO login(AuthRequestDTO.LoginRequest loginRequest);
    AuthResponseDTO.TokenResponse reissueToken(String refreshToken);
    AuthResponseDTO.LogoutResponse logout(String accessToken);
}
