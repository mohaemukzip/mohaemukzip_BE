package com.mohaemukzip.mohaemukzip_be.global.exception;

import com.mohaemukzip.mohaemukzip_be.global.response.code.BaseCode;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
    private final BaseCode baseCode;

    public GlobalException(BaseCode baseCode) {
        super(baseCode.getMessage());
        this.baseCode = baseCode;
    }
}
