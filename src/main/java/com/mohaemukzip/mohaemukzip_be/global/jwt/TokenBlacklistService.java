package com.mohaemukzip.mohaemukzip_be.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class TokenBlacklistService implements TokenBlacklistChecker {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "BL:";

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback: 원본 토큰 사용 (권장하지 않음)
            return token;
        }
    }
    public void addToBlacklist(String accessToken, long expirationMs) {
        if (expirationMs > 0) {
            String hashedToken = hashToken(accessToken);
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + hashedToken,
                    "logout",
                    expirationMs,
                    TimeUnit.MILLISECONDS
            );
        }
    }
    @Override
    public boolean isTokenBlacklisted(String accessToken) {
        String hashedToken = hashToken(accessToken);
        String blacklistedToken = redisTemplate.opsForValue().get(BLACKLIST_PREFIX + hashedToken);
        return blacklistedToken != null;
    }
}
