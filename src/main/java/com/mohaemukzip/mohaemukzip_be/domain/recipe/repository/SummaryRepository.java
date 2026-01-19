package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SummaryRepository extends JpaRepository<Summary,Long> {
    
    @Query("SELECT s FROM Summary s JOIN FETCH s.recipe WHERE s.title LIKE %:keyword%")
    List<Summary> findByTitleContaining(@Param("keyword") String keyword);
}
