package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class TermRequestDTO {
    @Schema(description = "약관 동의 요청")
    public record TermAgreementRequest(
            @Schema(description = "약관 ID", example = "1")
            @NotNull(message = "약관 ID는 필수입니다")
            Long termId,

            @Schema(description = "동의 여부", example = "true")
            @NotNull(message = "동의 여부는 필수입니다")
            Boolean isAgreed
    ) {}
    }
