package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.*;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.*;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class IngredientQueryServiceImpl implements IngredientQueryService {

    private final IngredientRequestRepository ingredientRequestRepository;
    private final MemberRecentSearchRepository memberRecentSearchRepository;
    private final MemberRepository memberRepository;
    private final IngredientRepository ingredientRepository;
    private final MemberIngredientRepository memberIngredientRepository;
    private final MemberFavoriteRepository memberFavoriteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponseDTO.Detail> getIngredients(Long memberId, String keyword, Category category) {

        List<Ingredient> ingredients;

        // 1. 키워드 + 카테고리가 있는 경우 조회
        if (keyword != null && !keyword.isBlank() && category != null) {
            ingredients = ingredientRepository.findByNameContainingAndCategory(keyword, category);
        }
        // 2. 키워드만 있는 경우
        else if (keyword != null && !keyword.isBlank()) {
            ingredients = ingredientRepository.findByNameContaining(keyword);
        }
        // 3. 카테고리만 있는 경우
        else if (category != null) {
            ingredients = ingredientRepository.findByCategory(category);
        }
        // 4. 둘 다 없는 경우 (전체 출력)
        else {
            ingredients = ingredientRepository.findAll();
        }

        // 로그인한 경우 즐겨찾기 ID 조회
        Set<Long> favoriteIngredientIds = new HashSet<>();
        if (memberId != null) {
            favoriteIngredientIds = memberFavoriteRepository.findIngredientIdsByMemberId(memberId);
        }

        // isFavorite 포함하여 DTO 변환
        final Set<Long> finalFavoriteIds = favoriteIngredientIds;
        return ingredients.stream()
                .map(ingredient -> IngredientResponseDTO.Detail.from(
                        ingredient,
                        finalFavoriteIds.contains(ingredient.getId())
                ))
                .collect(Collectors.toList());
    }



    // 냉장고 재료 조회
    @Override
    @Transactional(readOnly = true)
    public IngredientResponseDTO.FridgeIngredientList getMyFridgeIngredients(Long memberId) {

        List<MemberIngredient> memberIngredients = memberIngredientRepository.findAllByMemberId(memberId);

        List<IngredientResponseDTO.FridgeIngredient> dtoList = memberIngredients.stream()
                .map(IngredientResponseDTO.FridgeIngredient::from)
                .collect(toList());

        return IngredientResponseDTO.FridgeIngredientList.builder()
                .fridgeList(dtoList)
                .build();
    }

    //즐겨찾기 재료 리스트 조회
    @Override
    @Transactional(readOnly = true)
    public IngredientResponseDTO.FavoriteList getFavoriteList(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MemberFavorite> favoriteList = memberFavoriteRepository.findAllByMember(member);

        List<IngredientResponseDTO.FavoriteDetail> detailList = favoriteList.stream()
                .map(IngredientResponseDTO.FavoriteDetail::from)
                .toList();

        return new IngredientResponseDTO.FavoriteList(detailList);


    }

    @Override
    public IngredientResponseDTO.RecentSearchList getRecentSearch(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        List<MemberRecentSearch> recentSearches =
                memberRecentSearchRepository.findAllByMemberOrderByUpdatedAtDesc(member);

        return IngredientResponseDTO.RecentSearchList.from(recentSearches);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponseDTO.AdminRequestList> getIngredientRequestList() {
        List<IngredientRequest> requests = ingredientRequestRepository.findAllByOrderByCreatedAtDesc();

        return requests.stream()
                .map(IngredientResponseDTO.AdminRequestList::from)
                .toList();
    }


}