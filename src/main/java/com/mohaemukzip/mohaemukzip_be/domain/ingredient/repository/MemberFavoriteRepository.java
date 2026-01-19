package com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberFavorite;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberFavoriteRepository extends JpaRepository<MemberFavorite, Long> {

    boolean existsByMemberAndIngredient(Member member, Ingredient ingredient);
}
