package com.mohaemukzip.mohaemukzip_be.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenBlacklistService implements TokenBlacklistChecker {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "BL:";

    public void addToBlacklist(String accessToken, long expirationMs) {
        if (expirationMs > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + accessToken,
                    "logout",
                    expirationMs,
                    TimeUnit.MILLISECONDS
            );
        }
    }
    @Override
    public boolean isTokenBlacklisted(String accessToken) {
        String blacklistedToken = redisTemplate.opsForValue().get(BLACKLIST_PREFIX + accessToken);
        return blacklistedToken != null;
    }
}
