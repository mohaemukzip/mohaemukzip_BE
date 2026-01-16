package com.mohaemukzip.mohaemukzip_be.global.response.code.status;

import com.mohaemukzip.mohaemukzip_be.global.response.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {
    // Common Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러입니다. 관리자에게 문의하세요."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "찾을 수 없는 요청입니다."),

    // 인증관련 에러
    TOKEN_MISSING(HttpStatus.BAD_REQUEST, "AUTH4001", "Access Token 또는 Refresh Token이 누락되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH4002", "로그인 정보가 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH4003", "Refresh Token이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4004", "유효하지 않은 토큰입니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.FORBIDDEN, "AUTH4005", "Refresh Token이 일치하지 않습니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST, "AUTH4006", "중복된 로그인 아이디입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH4007", "비밀번호가 올바르지 않습니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4008", "토큰의 형식이 올바르지 않습니다."),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "AUTH4009", "토큰의 서명이 올바르지 않습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4010", "지원하지 않는 토큰 유형입니다."),
    ILLEGAL_ARGUMENT_TOKEN(HttpStatus.BAD_REQUEST, "AUTH4011", "토큰의 인수가 올바르지 않습니다."),
    TOKEN_PARSING_ERROR(HttpStatus.BAD_REQUEST, "AUTH4012", "토큰 파싱 중 오류가 발생했습니다."),

    // 멤버 관려 에러
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),
    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
