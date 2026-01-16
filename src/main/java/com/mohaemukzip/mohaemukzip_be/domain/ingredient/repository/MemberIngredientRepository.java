package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberIngredientRepository extends JpaRepository<MemberIngredient, Long> {
    List<MemberIngredient> findAllByMemberId(Long memberId);
}
