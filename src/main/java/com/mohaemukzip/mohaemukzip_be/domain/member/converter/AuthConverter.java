package com.mohaemukzip.mohaemukzip_be.domain.member.converter;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;

public class AuthConverter {
    public static AuthResponseDTO.GetUserDTO toSignupResponseDTO(
            Member member,
            String accessToken,
            String refreshToken,
            boolean isNewUser) {
        return AuthResponseDTO.GetUserDTO.of(member, accessToken, refreshToken, isNewUser);
    }

    public static AuthResponseDTO.TokenResponse toTokenResponseDTO(
            String accessToken,
            String refreshToken) {
        return AuthResponseDTO.TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
