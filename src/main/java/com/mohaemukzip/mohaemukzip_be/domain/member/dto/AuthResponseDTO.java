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

    public record CheckLoginIdResponse(
            boolean available,
            String message
    ) {
        public static CheckLoginIdResponse ofAvailable() {
            return new CheckLoginIdResponse(true, "사용 가능한 아이디예요.");
        }

        public static CheckLoginIdResponse ofNotAvailable() {
            return new CheckLoginIdResponse(false, "이미 사용중인 아이디예요. 다른 아이디를 입력해 주세요.");
        }
    }

    public record LogoutResponse(
            String message
    ) { }
}
