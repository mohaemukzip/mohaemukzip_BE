package com.mohaemukzip.mohaemukzip_be.domain.member.repository;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.MemberTerm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermRepository extends JpaRepository<MemberTerm,Long> {
    void deleteAllByMember(Member member);
}
