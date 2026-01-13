package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthRequestDTO {
    public record LoginRequest(
            @NotBlank
            String loginId,

            @NotBlank
            String password
    ) { }

    public record SignUpRequest(
            @NotBlank(message = "닉네임은 필수입니다")
            String nickname,

            @NotBlank(message = "아이디는 필수입니다")
            @Pattern(regexp = "^(?=.*[A-Za-z])[A-Za-z\\d]{4,20}$", message = "아이디는 영문과 숫자를 사용해 4자 이상 입력해주세요.")
            String loginId,

            @NotBlank(message = "비밀번호는 필수입니다")
            @Pattern(
                    regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{10,}$",
                    message = "비밀번호는 영문과 숫자를 포함해 10자 이상 입력해 주세요."
            )
            String password
    ) { }
}
