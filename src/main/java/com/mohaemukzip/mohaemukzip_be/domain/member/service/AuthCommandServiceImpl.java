package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.enums.LoginType;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.enums.Role;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.converter.AuthConverter;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.jwt.JwtProvider;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCommandServiceImpl implements AuthCommandService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    @Transactional
    public AuthResponseDTO.GetUserDTO signup(AuthRequestDTO.SignUpRequest request) {

        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorStatus.DUPLICATE_LOGIN_ID);
        }

        Member member = Member.builder()
                .nickname(request.nickname())
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .loginType(LoginType.GENERAL)
                .level(0)
                .score(0)
                .build();
        memberRepository.save(member);

        log.info("✅ 회원 저장 완료 - memberId: {}", member.getId());

        return generateAndSaveTokens(member, true);
    }

    public AuthResponseDTO.GetUserDTO login(AuthRequestDTO.LoginRequest request) {
        log.info("🔐 로그인 시도 - loginId: {}", request.loginId());

        Member member = memberRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorStatus.INVALID_PASSWORD);
        }
        log.info("✅ 로그인 성공 - memberId: {}", member.getId());
        return generateAndSaveTokens(member, false);
    }

    private AuthResponseDTO.GetUserDTO generateAndSaveTokens(Member member, boolean isNewUser) {

        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(member);

        saveRefreshTokenToRedis(member.getId().toString(), refreshToken);

        return AuthConverter.toSignupResponseDTO(member, accessToken, refreshToken, isNewUser);
    }
    /**
     * Refresh Token을 Redis에 저장
     */
    private void saveRefreshTokenToRedis(String userId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        log.info("💾 Redis 저장 시도 - key: {}, token 길이: {}", REFRESH_TOKEN_PREFIX + userId, refreshToken.length());

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                JwtProvider.REFRESH_TOKEN_VALIDITY_MS,
                TimeUnit.MILLISECONDS
        );

        // 저장 확인
        String saved = redisTemplate.opsForValue().get(key);
        if (saved != null) {
            log.info("✅ Redis 저장 성공 - key: {}", key);
        } else {
            log.error("❌ Redis 저장 실패 - key: {}", key);
        }
    }

    private String getUserIdFromToken(String token) {
        if (!jwtProvider.validateToken(token)) {
            throw new BusinessException(ErrorStatus.INVALID_TOKEN);
        }
        return jwtProvider.getUserIdFromToken(token);
    }

    private void validateRefreshToken(String memberId, String clientRefreshToken) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        log.info("🔍 Refresh Token 검증 - key: {}", key);

        String storedRefreshToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + memberId);

        if (storedRefreshToken == null) {
            log.error("❌ Redis에 토큰 없음 - key: {}", key);
            throw new BusinessException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND);
        }

        if (!storedRefreshToken.equals(clientRefreshToken)) {
            // 토큰 불일치 시 탈취로 간주하고 즉시 삭제
            log.error("❌ 토큰 불일치 - key: {}", key);
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + memberId);
            throw new BusinessException(ErrorStatus.REFRESH_TOKEN_MISMATCH);
        }

        // RT 자체의 만료 시간 검증
        if (!jwtProvider.validateToken(clientRefreshToken)) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + memberId);
            throw new BusinessException(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }
        log.info("✅ Refresh Token 검증 성공 - key: {}", key);
    }

    @Transactional
    public AuthResponseDTO.TokenResponse reissueToken(String refreshToken) {
        log.info("🔄 토큰 재발급 시작");
        String userId = getUserIdFromToken(refreshToken);
        validateRefreshToken(userId, refreshToken);

        Member member = memberRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        String newAccessToken = jwtProvider.generateAccessToken(member);
        String newRefreshToken = jwtProvider.generateRefreshToken(member);
        saveRefreshTokenToRedis(userId, newRefreshToken);
        log.info("✅ 토큰 재발급 완료 - memberId: {}", userId);
        return AuthConverter.toTokenResponseDTO(newAccessToken, newRefreshToken);
    }

}
