package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthRequestDTO {
    @Schema(description = "로그인 요청")
    public record LoginRequest(
            @Schema(description = "로그인 ID", example = "test01")
            @NotBlank
            String loginId,

            @Schema(description = "비밀번호", example = "Test1234!")
            @NotBlank
            String password
    ) { }

    public record SignUpRequest(
            @Schema(description = "닉네임", example = "뭐해먹집")
            @NotBlank(message = "닉네임은 필수입니다")
            String nickname,

            @Schema(description = "로그인 ID", example = "test01")
            @NotBlank(message = "아이디는 필수입니다")
            @Pattern(regexp = "^(?=.*[A-Za-z])[A-Za-z\\d]{4,20}$", message = "아이디는 영문과 숫자를 사용해 4자 이상 입력해주세요.")
            String loginId,

            @Schema(description = "비밀번호", example = "Test1234!")
            @NotBlank(message = "비밀번호는 필수입니다")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{10,}$",
                    message = "비밀번호는 영문과 숫자를 포함해 10자 이상 입력해 주세요."
            )
            String password
    ) { }

    public record CheckLoginIdRequest(
            @Schema(description = "아이디", example = "test01")
            @NotBlank(message = "아이디를 입력해주세요.")
            @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$",
                    message = "아이디는 영문, 숫자 4-20자로 입력해주세요.")
            String loginId
    ) {}
}
