package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;

public interface MemberQueryService {
    MemberResponseDTO.GetMemberDTO getMyProfile(Long memberId);
}
