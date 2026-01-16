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

    public static AuthResponseDTO.LogoutResponse toLogoutResponseDTO() {
        return new AuthResponseDTO.LogoutResponse("로그아웃되었습니다.");
    }

    public static AuthResponseDTO.WithdrawalResponse toWithdrawalResponseDTO() {
        return new AuthResponseDTO.WithdrawalResponse("회원 탈퇴가 완료되었습니다.");
    }
}
