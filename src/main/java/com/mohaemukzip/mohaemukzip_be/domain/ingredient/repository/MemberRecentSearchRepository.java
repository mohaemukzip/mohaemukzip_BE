package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberRecentSearch;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
public interface MemberRecentSearchRepository extends JpaRepository<MemberRecentSearch, Long> {

    Optional<MemberRecentSearch> findByMemberAndKeyword(Member member, String keyword);

    List<MemberRecentSearch> findAllByMemberOrderByUpdatedAtDesc(Member member);


    Optional<MemberRecentSearch> findByIdAndMemberId(Long id, Long memberId);

    @Modifying
    @Query("UPDATE MemberRecentSearch m SET m.updatedAt = :now WHERE m.id = :id")
    void updateUpdatedAt(@Param("id") Long id, @Param("now") LocalDateTime now);
}