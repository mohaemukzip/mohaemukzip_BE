package com.mohaemukzip.mohaemukzip_be.domain.Member.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,Long> {
}
