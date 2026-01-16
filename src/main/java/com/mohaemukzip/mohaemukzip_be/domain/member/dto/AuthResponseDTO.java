package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import lombok.Builder;

public class AuthResponseDTO {

    public record GetUserDTO(
            Long id,
            String accessToken,
            String refreshToken,
            boolean isNewUser,
            boolean isInactive
    ) {
        public static GetUserDTO of(Member member, String accessToken, String refreshToken , boolean isNewUser) {
            return new GetUserDTO(
                    member.getId(),
                    accessToken,
                    refreshToken,
                    isNewUser,
                    member.isInactive()
            );
        }
    }

    public record TokenResponse(
            String accessToken,
            String refreshToken
    ) {
        @Builder
        public TokenResponse {}
    }

    public record LogoutResponse(
            String message
    ) { }
}
