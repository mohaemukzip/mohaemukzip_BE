package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.dto.TermRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;

import java.util.List;

public interface TermCommandService {
    void createMemberTerms(Member member, List<TermRequestDTO.TermAgreementRequest> termAgreements);
    AuthResponseDTO.GetUserDTO updateMemberTerms(Long memberId, List<TermRequestDTO.TermAgreementRequest> terms);
}
