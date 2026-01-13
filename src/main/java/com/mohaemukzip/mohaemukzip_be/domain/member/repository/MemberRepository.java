package com.mohaemukzip.mohaemukzip_be.domain.member.repository;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findById(Long id);
    Optional<Member> findByOauthId(Long oauthId);
    Boolean existsByOauthId(Long oauthId);
    Optional<Member> findByLoginId(String loginId);
    Boolean existsByLoginId(String loginId);
}
