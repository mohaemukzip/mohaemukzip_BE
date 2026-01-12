package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import lombok.Builder;

public class AuthResponseDTO {

    public record LoginResponse(
            Long id,
            String accessToken,
            String refreshToken,
            boolean isNewUser,
            boolean isInactive
    ) {
        @Builder
        public LoginResponse {}

        public static LoginResponse of(Member member, String accessToken, String refreshToken , boolean isNewUser) {
            return LoginResponse.builder()
                    .id(member.getId())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .isNewUser(isNewUser)
                    .isInactive(member.isInactive())
                    .build();
        }
    }
}
