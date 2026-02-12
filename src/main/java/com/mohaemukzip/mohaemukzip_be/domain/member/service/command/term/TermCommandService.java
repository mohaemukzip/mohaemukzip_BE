package com.mohaemukzip.mohaemukzip_be.domain.member.service.command.term;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.TermRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;

import java.util.List;

public interface TermCommandService {
    void createMemberTerms(Member member, List<TermRequestDTO.TermAgreementRequest> termAgreements);
    void updateMemberTerms(Long memberId, List<TermRequestDTO.TermAgreementRequest> terms);
}
