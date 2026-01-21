package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;


import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.IngredientRequest;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IngredientRequestRepository extends JpaRepository<IngredientRequest, Long> {


    Optional<IngredientRequest> findByMemberAndIngredientName(Member member, String ingredientName);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE IngredientRequest ir SET ir.updatedAt = :now WHERE ir.id = :id")
    void updateUpdatedAt(@Param("id") Long id, @Param("now") LocalDateTime now);
}