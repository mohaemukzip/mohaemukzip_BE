package com.mohaemukzip.mohaemukzip_be.global.jwt;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.AuthResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORITIES_KEY = "auth";

    // 30분 (1000L * 60 * 30)
    private static final long ACCESS_TOKEN_VALIDITY_MS = 1800000L;
    // 7일 (1000L * 60 * 60 * 24 * 7)
    public static final long REFRESH_TOKEN_VALIDITY_MS = 604800000L;

    private final Key key;
    private final UserDetailsService userDetailsService;
    private final JwtParser jwtParser;


    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            UserDetailsService userDetailsService
    ) {
        this.userDetailsService = userDetailsService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        this.jwtParser = Jwts.parser()
                .setSigningKey(key)
                .build();
    }

    public String generateAccessToken(Member member) {
        String authorities = member.getRole().name();
        long now = System.currentTimeMillis();
        Date expires = new Date(now + ACCESS_TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .setSubject(String.valueOf(member.getId()))
                .claim(AUTHORITIES_KEY, authorities)
                .setIssuedAt(new Date())
                .setExpiration(expires)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Member member) {
        long now = System.currentTimeMillis();
        Date expires = new Date(now + REFRESH_TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .setSubject(String.valueOf(member.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expires)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = jwtParser
                .parseClaimsJws(token)
                .getBody();

        // 클레임에서 User ID (Subject) 추출
        String userId = claims.getSubject();

        // UserDetailsService를 사용하여 UserDetails 객체 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

        // UserDetails, 토큰, 권한 정보로 Authentication 객체 생성
        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature");
            throw new BusinessException(ErrorStatus.INVALID_TOKEN);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token");
            throw new BusinessException(ErrorStatus.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
            throw new BusinessException(ErrorStatus.INVALID_TOKEN);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
            throw new BusinessException(ErrorStatus.INVALID_TOKEN);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty");
            throw new BusinessException(ErrorStatus.INVALID_TOKEN);
        }
    }

    // HttpServletRequest에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public Long getRemainingExpiration(String token) {
        try {
            Claims claims = jwtParser
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long now = new Date().getTime();

            return (expiration.getTime() - now);
        } catch (Exception e) {
            log.error("토큰 만료 시간 파싱 중 오류 발생: {}", e.getMessage());
            return 0L;
        }
    }

    /**
     * 만료된 토큰에서도 User ID 추출 (재발급용)
     */
    public String getUserIdFromToken(String token) {
        try {
            return jwtParser
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // 토큰이 만료되었어도 Subject(userId)는 추출
            return e.getClaims().getSubject();
        } catch (Exception e) {
            log.error("토큰에서 User ID 추출 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorStatus.INVALID_TOKEN);
        }

    }
}