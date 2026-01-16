package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Term;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class TermResponseDTO {

    @Schema(description = "약관 목록 응답")
    public record TermListResponse(
            @Schema(description = "약관 목록")
            List<TermInfo> terms
    ) {
        public static TermListResponse from(List<Term> terms) {
            return new TermListResponse(
                    terms.stream()
                            .map(TermInfo::from)
                            .toList()
            );
        }
    }

    @Schema(description = "약관 정보")
    public record TermInfo(
            @Schema(description = "약관 ID", example = "1")
            Long id,

            @Schema(description = "약관명", example = "서비스 이용약관")
            String termName,

            @Schema(description = "필수 여부", example = "true")
            Boolean isRequired
    ) {
        public static TermInfo from(Term term) {
            return new TermInfo(
                    term.getId(),
                    term.getTitle(),
                    term.isRequired()
            );
        }
    }
}
