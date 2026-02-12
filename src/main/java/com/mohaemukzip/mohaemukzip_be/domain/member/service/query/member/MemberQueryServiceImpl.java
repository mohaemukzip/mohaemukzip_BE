package com.mohaemukzip.mohaemukzip_be.domain.member.service.query.member;

import com.mohaemukzip.mohaemukzip_be.domain.member.converter.MemberConverter;
import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.MemberRecipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.MemberRecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.service.RecentlyViewedRecipeManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService {
    private final MemberRepository memberRepository;
    private final RecentlyViewedRecipeManager recentlyViewedRecipeManager;
    private final RecipeRepository recipeRepository;
    private final MemberRecipeRepository memberRecipeRepository;
    private final MemberConverter memberConverter;

    private static final int PAGE_SIZE = 10;
    private static final int MYPAGE_PREVIEW_SIZE = 3;

    @Override
    public MemberResponseDTO.MyPageDTO getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        List<RecipeResponseDTO.RecipePreviewDTO> recentlyViewed =
                getRecentlyViewedRecipesPreview(memberId);

        List<RecipeResponseDTO.RecipePreviewDTO> bookmarked =
                getBookmarkedRecipesPreview(memberId);

        return memberConverter.toMyPageDTO(member, recentlyViewed, bookmarked);
    }

    @Override
    public List<RecipeResponseDTO.RecipePreviewDTO> getRecentlyViewedRecipes(Long memberId) {
        // Redis에서 레시피 ID 목록 조회
        List<Long> recipeIds = recentlyViewedRecipeManager.getList(memberId,MYPAGE_PREVIEW_SIZE);

        if (recipeIds.isEmpty()) {
            return Collections.emptyList();
        }

        return convertRecipeIdsToPreviewDTOs(recipeIds, memberId);
    }

    @Override
    public RecipeResponseDTO.RecipePreviewListDTO getBookmarkedRecipes(Long memberId, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<MemberRecipe> memberRecipePage = memberRecipeRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId, pageable);

        Set<Long> bookmarkedRecipeIds = memberRecipePage.getContent()
                .stream()
                .map(mr -> mr.getRecipe().getId())
                .collect(Collectors.toSet());

        Page<Recipe> recipePage = memberRecipePage.map(MemberRecipe::getRecipe);

        return RecipeConverter.toRecipePreviewListDTO(recipePage, bookmarkedRecipeIds);
    }

//    저장된 레시피 미리보기 (최대 3개)
    private List<RecipeResponseDTO.RecipePreviewDTO> getBookmarkedRecipesPreview(Long memberId) {
        Pageable pageable = PageRequest.of(0, MYPAGE_PREVIEW_SIZE);

        Page<MemberRecipe> memberRecipePage = memberRecipeRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId, pageable);

        if (memberRecipePage.isEmpty()) {
            return Collections.emptyList();
        }

        return memberRecipePage.getContent()
                .stream()
                .map(memberRecipe -> RecipeConverter.toRecipePreviewDTO(memberRecipe.getRecipe(), true))
                .collect(Collectors.toList());
    }

//    최근 본 레시피 미리보기 (최대 3개)
    private List<RecipeResponseDTO.RecipePreviewDTO> getRecentlyViewedRecipesPreview(Long memberId) {
        List<Long> recipeIds = recentlyViewedRecipeManager.getList(memberId, MYPAGE_PREVIEW_SIZE);

        if (recipeIds.isEmpty()) {
            return Collections.emptyList();
        }

        return convertRecipeIdsToPreviewDTOs(recipeIds, memberId);
    }

//    레시피 ID 목록을 PreviewDTO 목록으로 변환
    private List<RecipeResponseDTO.RecipePreviewDTO> convertRecipeIdsToPreviewDTOs(
            List<Long> recipeIds,
            Long memberId
    ) {
        Map<Long, Recipe> recipeMap = recipeRepository.findAllById(recipeIds)
                .stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));

        Set<Long> bookmarkedRecipeIds = memberRecipeRepository
                .findBookmarkedRecipeIdsByMemberId(memberId, recipeIds);

        return recipeIds.stream()
                .map(recipeId -> {
                    Recipe recipe = recipeMap.get(recipeId);
                    if (recipe == null) {
                        log.warn("Recipe not found in DB but exists in Redis: recipeId={}", recipeId);
                        return null;
                    }
                    Boolean isBookmarked = bookmarkedRecipeIds.contains(recipeId);
                    return RecipeConverter.toRecipePreviewDTO(recipe, isBookmarked);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

