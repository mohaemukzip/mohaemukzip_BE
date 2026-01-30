package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.*;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.*;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientCommandServiceImpl implements IngredientCommandService {

    private final IngredientRequestRepository  ingredientRequestRepository;
    private final IngredientRepository ingredientRepository;
    private final MemberRepository memberRepository;
    private final RecentSearchService recentSearchService;
    private final MemberIngredientRepository memberIngredientRepository;
    private final MemberFavoriteRepository memberFavoriteRepository;

    private static final int MAX_RECENT_SEARCH_COUNT = 10;

    @Override
    public IngredientResponseDTO.AddFridgeResult addFridgeIngredient(Long memberId, IngredientRequestDTO.AddFridge request) {

        // 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        // 재료 조회
        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new BusinessException(ErrorStatus.INGREDIENT_NOT_FOUND));

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

    @Override
    public IngredientResponseDTO.DeleteFridgeIngredient deleteIngredient(Long memberIngredientId, Long memberId) {

        MemberIngredient memberIngredient = memberIngredientRepository.findByIdAndMemberId(memberIngredientId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.INGREDIENT_NOT_FOUND));

        memberIngredientRepository.delete(memberIngredient);

        return new IngredientResponseDTO.DeleteFridgeIngredient(memberIngredient.getId());
    }

    @Override
    @Transactional
    public IngredientResponseDTO.ToggleFavorite toggleFavorite(Long memberId, Long ingredientId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.INGREDIENT_NOT_FOUND));

        Optional<MemberFavorite> existingFavorite =
                memberFavoriteRepository.findByMemberAndIngredient(member, ingredient);

        if (existingFavorite.isPresent()) {
            // 이미 존재하면 삭제
            MemberFavorite favorite = existingFavorite.get();
            memberFavoriteRepository.delete(favorite);

            return IngredientResponseDTO.ToggleFavorite.builder()
                    .memberFavoriteId(favorite.getId())
                    .ingredientId(ingredientId)
                    .isFavorite(false)
                    .build();
        } else {
            // 존재하지 않으면 등록
            MemberFavorite memberFavorite = MemberFavorite.builder()
                    .member(member)
                    .ingredient(ingredient)
                    .build();

            MemberFavorite saved = memberFavoriteRepository.save(memberFavorite);

            return IngredientResponseDTO.ToggleFavorite.builder()
                    .memberFavoriteId(saved.getId())
                    .ingredientId(ingredientId)
                    .isFavorite(true)
                    .build();
        }
    }




    @Override
    @Transactional
    public void createIngredientRequest(Long memberId, IngredientRequestDTO.IngredientReq request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        Optional<IngredientRequest> existingRequest =
                ingredientRequestRepository.findByMemberAndIngredientName(member, request.getIngredientName());

        if (existingRequest.isPresent()) {
            throw new BusinessException(ErrorStatus.INGREDIENT_ALREADY_REQUESTED);
        }

        IngredientRequest newRequest = IngredientRequest.builder()
                .member(member)
                .ingredientName(request.getIngredientName())
                .build();

        ingredientRequestRepository.save(newRequest);
    }
}
