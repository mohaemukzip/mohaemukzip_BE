package com.mohaemukzip.mohaemukzip_be.domain.member.service.command.member;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberRequestDTO;

public interface MemberCommandService {
    void updateProfile(Long memberId, MemberRequestDTO.ProfileUpdateRequest req);
}
