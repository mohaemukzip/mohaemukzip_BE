package com.mohaemukzip.mohaemukzip_be.global.jwt;

import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final TokenBlacklistChecker tokenBlacklistChecker;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Request Header에서 토큰 추출
        String token = jwtProvider.resolveToken(request);

        // 2. 토큰 유효성 검사
        if (token != null) {

            // 2-1. 블랙리스트 확인 (로그아웃된 토큰인지 체크)
            if (tokenBlacklistChecker.isTokenBlacklisted(token)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }

            // 2-2. 토큰 유효성 검사
            if (jwtProvider.validateToken(token)) {
                // 3. 토큰이 유효할 경우, 토큰에서 Authentication 객체를 가져와 SecurityContext에 저장
                try {
                    Authentication authentication = jwtProvider.getAuthentication(token);
                    // 비활성화된 회원인지 확인
                    if (authentication.getPrincipal() instanceof CustomUserDetails) {
                        CustomUserDetails userDetails =
                                (CustomUserDetails) authentication.getPrincipal();

                        if (userDetails.getMember().isInactive()) {
                            SecurityContextHolder.clearContext();
                            filterChain.doFilter(request, response);
                            return;
                        }
                    }
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (UsernameNotFoundException ex) {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
        }

        // 4. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}
