package com.mohaemukzip.mohaemukzip_be.global.exception;

import com.mohaemukzip.mohaemukzip_be.global.response.code.BaseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final BaseCode baseCode;

    public BusinessException(BaseCode baseCode) {
        super(baseCode.getMessage());
        this.baseCode = baseCode;
    }
}
