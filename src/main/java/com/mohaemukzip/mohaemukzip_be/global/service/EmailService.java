package com.mohaemukzip.mohaemukzip_be.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String EMAIL_AUTH_PREFIX = "EMAIL_AUTH:";
    private static final long EMAIL_AUTH_TTL = 3;

    public void sendAuthCode(String email) {
        String authCode = generateAuthCode();

        // Redis에 저장 (TTL 3분)
        redisTemplate.opsForValue().set(
                EMAIL_AUTH_PREFIX + email,
                authCode,
                EMAIL_AUTH_TTL,
                TimeUnit.MINUTES
        );

        // 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[뭐해먹집] 이메일 인증번호");
        message.setText("인증번호: " + authCode + "\n\n5분 안에 입력해주세요.");
        mailSender.send(message);

        log.info("인증번호 발송 완료 - email: {}", email);
    }

    public boolean verifyAuthCode(String email, String inputCode) {
        String storedCode = redisTemplate.opsForValue().get(EMAIL_AUTH_PREFIX + email);

        if (storedCode == null) {
            return false; // 만료
        }

        boolean matched = storedCode.equals(inputCode);
        if (matched) {
            redisTemplate.delete(EMAIL_AUTH_PREFIX + email);
        }
        return matched;
    }

    private String generateAuthCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}
