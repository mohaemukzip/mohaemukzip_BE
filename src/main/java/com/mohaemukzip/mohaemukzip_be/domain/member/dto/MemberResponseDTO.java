package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

public class MemberResponseDTO {
    public record GetMemberDTO(
            String profileImageUrl,
            String nickname,
            Integer level,
            Integer remainingScore
    ) { }
}
