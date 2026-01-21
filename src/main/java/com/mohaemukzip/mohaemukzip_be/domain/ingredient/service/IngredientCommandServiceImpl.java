package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberFavorite;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberRecentSearch;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberFavoriteRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberRecentSearchRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientCommandServiceImpl implements IngredientCommandService {

    private final IngredientRepository ingredientRepository;
    private final MemberRepository memberRepository;
    private final MemberRecentSearchRepository memberRecentSearchRepository;
    private final MemberIngredientRepository memberIngredientRepository;
    private final MemberFavoriteRepository memberFavoriteRepository;

    private static final int MAX_RECENT_SEARCH_COUNT = 20;

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
    public IngredientResponseDTO.AddFavorite addFavorite(Long memberId, Long ingredientId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.INGREDIENT_NOT_FOUND));

        if (memberFavoriteRepository.existsByMemberAndIngredient(member, ingredient)) {
            throw new BusinessException(ErrorStatus.ALREADY_FAVORITE);
        }

        MemberFavorite memberFavorite = MemberFavorite.builder()
                .member(member)
                .ingredient(ingredient)
                .build();

        MemberFavorite saved = memberFavoriteRepository.save(memberFavorite);

        return new IngredientResponseDTO.AddFavorite(saved.getId(), ingredient.getId());
    }

    @Override
    public IngredientResponseDTO.DeleteFavorite deleteFavorite(Long memberId, Long favoriteId) {

        MemberFavorite favorite = memberFavoriteRepository.findByIdAndMemberId(favoriteId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.FAVORITE_NOT_FOUND));

        memberFavoriteRepository.delete(favorite);

        return new IngredientResponseDTO.DeleteFavorite(favorite.getId());

    }

    //최근 검색어 저장
    @Override
    @Transactional
    public void saveRecentSearch(Long memberId, String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return;
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));


        Optional<MemberRecentSearch> existingSearch =
                memberRecentSearchRepository.findByMemberAndKeyword(member, keyword);

        if (existingSearch.isPresent()) {
            memberRecentSearchRepository.updateUpdatedAt(
                    existingSearch.get().getId(),
                    LocalDateTime.now()
            );
            return;
        }
        MemberRecentSearch newSearch = MemberRecentSearch.builder()
                .member(member)
                .keyword(keyword)
                .build();

        memberRecentSearchRepository.save(newSearch);

        List<MemberRecentSearch> allSearches =
                memberRecentSearchRepository.findAllByMemberOrderByUpdatedAtDesc(member);

        if (allSearches.size() > MAX_RECENT_SEARCH_COUNT) {

            List<MemberRecentSearch> searchesToDelete =
                    allSearches.subList(MAX_RECENT_SEARCH_COUNT, allSearches.size());

            memberRecentSearchRepository.deleteAllInBatch(searchesToDelete);

        }

    }

    @Override
    @Transactional
    public void deleteRecentSearch(Long memberId, Long recentSearchId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        MemberRecentSearch search = memberRecentSearchRepository.findById(recentSearchId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.SEARCH_NOT_FOUND));


        if (!search.getMember().getId().equals(member.getId())) {
            throw new BusinessException(ErrorStatus.MEMBER_NOT_MATCH);
            // 💡 남의 걸 지우려고 할 때 띄울 에러 (FORBIDDEN 관련)
        }

        memberRecentSearchRepository.delete(search);

    }
}
