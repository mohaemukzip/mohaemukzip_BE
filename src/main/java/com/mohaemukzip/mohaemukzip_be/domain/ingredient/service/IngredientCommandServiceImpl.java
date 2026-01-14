package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientCommandServiceImpl implements IngredientCommandService {

    private final IngredientRepository ingredientRepository;
    private final MemberRepository memberRepository;
    private final MemberIngredientRepository memberIngredientRepository;


    @Override
    public IngredientResponseDTO.AddFridgeResult addFridgeIngredient(Long memberId, IngredientRequestDTO.AddFridge request) {

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. id=" + memberId));

        // 재료 조회
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다. id=" + request.getIngredientId()));

        //엔티티 생성
        MemberIngredient memberIngredient = MemberIngredient.builder()
                .member(member)
                .ingredient(ingredient)
                .storageType(request.getStorageType()) // 냉장/냉동/실온
                .expireDate(request.getExpireDate())   // 유통기한
                .weight(request.getWeight())           // 용량
                .build();

        // DB에 insert
        MemberIngredient saved = memberIngredientRepository.save(memberIngredient);

        return IngredientResponseDTO.AddFridgeResult.builder()
                .memberIngredientId(saved.getId())
                .build();

    }
}
