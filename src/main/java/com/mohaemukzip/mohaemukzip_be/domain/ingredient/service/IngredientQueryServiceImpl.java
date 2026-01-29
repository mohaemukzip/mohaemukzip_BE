package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.*;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.*;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class IngredientQueryServiceImpl implements IngredientQueryService {

    private static final int PAGE_SIZE = 20;
    private final RecentSearchService recentSearchService;
    private final IngredientRequestRepository ingredientRequestRepository;
    private final MemberRepository memberRepository;
    private final IngredientRepository ingredientRepository;
    private final MemberIngredientRepository memberIngredientRepository;
    private final MemberFavoriteRepository memberFavoriteRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<IngredientResponseDTO.Detail> getIngredients(Long memberId, String keyword, Category category, Integer page) {

        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword : null;

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Ingredient> ingredients = ingredientRepository
                .findByKeywordAndCategory(searchKeyword, category, pageable);

        // 로그인한 경우 즐겨찾기 ID 조회
        Set<Long> favoriteIngredientIds = (memberId != null)
                ? memberFavoriteRepository.findIngredientIdsByMemberId(memberId)
                : Collections.emptySet();

        return ingredients.map(ingredient -> IngredientResponseDTO.Detail.from(
                ingredient,
                favoriteIngredientIds.contains(ingredient.getId())
        ));
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
    public List<IngredientResponseDTO.Detail> getFavoriteList(Long memberId) {

        List<MemberFavorite> favoriteList = memberFavoriteRepository.findAllByMemberId(memberId);

        return favoriteList.stream()
                .map(favorite -> IngredientResponseDTO.Detail.from(
                        favorite.getIngredient(),true
                ))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<IngredientResponseDTO.AdminRequestList> getIngredientRequestList() {
        List<IngredientRequest> requests = ingredientRequestRepository.findAllByOrderByCreatedAtDesc();

        return requests.stream()
                .map(IngredientResponseDTO.AdminRequestList::from)
                .toList();
    }

    // 최근 검색어 조회
    @Override
    public IngredientResponseDTO.RecentSearchList getRecentSearches(Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorStatus.MEMBER_NOT_FOUND);
        }

        List<String> keywords = recentSearchService.getRecentSearches(memberId);

        return IngredientResponseDTO.RecentSearchList.from(keywords);
    }


}