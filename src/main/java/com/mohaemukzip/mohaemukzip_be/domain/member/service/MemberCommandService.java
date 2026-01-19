package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberRequestDTO;

public interface MemberCommandService {
    void updateProfile(Long memberId, MemberRequestDTO.ProfileUpdateRequest req);
}
