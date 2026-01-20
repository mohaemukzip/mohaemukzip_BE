package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberRecentSearch;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRecentSearchRepository extends JpaRepository<MemberRecentSearch, Long> {

    Optional<MemberRecentSearch> findByMemberAndKeyword(Member member, String keyword);

    List<MemberRecentSearch> findAllByMemberOrderByCreatedAtDesc(Member member);

    Long countByMember(Member member);

    //제일 오래된 검색어 한개 찾기 (삭제용)
    MemberRecentSearch findTopByMemberOrderByCreatedAtAsc(Member member);
}
