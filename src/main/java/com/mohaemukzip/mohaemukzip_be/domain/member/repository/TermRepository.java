package com.mohaemukzip.mohaemukzip_be.domain.member.repository;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermRepository extends JpaRepository<Term,Long> {
    List<Term> findByIsActiveTrue();

    List<Term> findByIsActiveTrueAndIsRequiredTrue();
}
