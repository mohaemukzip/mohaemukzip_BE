package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.TermResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Term;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermQueryServiceImpl implements TermQueryService {
    private final TermRepository termRepository;

    @Transactional(readOnly = true)
    public TermResponseDTO.TermListResponse getTerms() {
        List<Term> terms = termRepository.findByIsActiveTrue();
        return TermResponseDTO.TermListResponse.from(terms);
    }
}
