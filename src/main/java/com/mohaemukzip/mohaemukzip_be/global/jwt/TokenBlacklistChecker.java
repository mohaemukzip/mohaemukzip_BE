package com.mohaemukzip.mohaemukzip_be.global.jwt;

public interface TokenBlacklistChecker {
    boolean isTokenBlacklisted(String token);
}
