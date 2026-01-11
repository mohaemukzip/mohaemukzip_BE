package com.mohaemukzip.mohaemukzip_be.domain.Member.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Member.entity.Member_Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberTermRepository extends JpaRepository<Member_Term,Long> {
}
