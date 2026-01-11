package com.mohaemukzip.mohaemukzip_be.domain.Recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.Recipe.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummaryRepository extends JpaRepository<Summary,Long> {
}
