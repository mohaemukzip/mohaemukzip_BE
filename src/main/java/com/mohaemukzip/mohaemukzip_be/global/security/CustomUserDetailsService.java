package com.mohaemukzip.mohaemukzip_be.global.security;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {

        Long id;
        try {
            id = Long.parseLong(memberId);
        } catch (NumberFormatException e) {
            log.error("Invalid member ID format: {}", memberId);
            throw new UsernameNotFoundException("Invalid member ID format.");
        }

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found with id: " + id));

        return new CustomUserDetails(member);
    }
}
