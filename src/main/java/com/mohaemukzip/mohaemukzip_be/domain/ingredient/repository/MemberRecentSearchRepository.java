package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberRecentSearch;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRecentSearchRepository extends JpaRepository<MemberRecentSearch, Long> {

    Optional<MemberRecentSearch> findByMemberAndKeyword(Member member, String keyword);

    List<MemberRecentSearch> findAllByMemberOrderByUpdatedAtDesc(Member member);

    @Modifying
    @Query("UPDATE MemberRecentSearch m SET m.updatedAt = :now WHERE m.id = :id")
    void updateUpdatedAt(@Param("id") Long id, @Param("now") LocalDateTime now);


    Optional<MemberRecentSearch> findByIdAndMemberId(Long id, Long memberId);

}
