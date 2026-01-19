package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class MemberRequestDTO {
    public record ProfileUpdateRequest(
            @Schema(description = "프로필 이미지 key", example = "profiles/uuid.png")
            String profileImageKey,

            @Schema(description = "닉네임", example = "뭐해먹집")
            String nickname
    ) {}
}
