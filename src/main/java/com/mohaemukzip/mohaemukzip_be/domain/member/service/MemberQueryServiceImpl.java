package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.MemberRecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.s3.S3Service;
import com.mohaemukzip.mohaemukzip_be.global.service.LevelService;
import com.mohaemukzip.mohaemukzip_be.global.service.RecentlyViewedRecipeManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService {
    private final MemberRepository memberRepository;
    private final LevelService levelService;
    private final S3Service s3Service;
    private final RecentlyViewedRecipeManager recentlyViewedRecipeManager;
    private final RecipeRepository recipeRepository;
    private final MemberRecipeRepository memberRecipeRepository;

    @Override
    public MemberResponseDTO.GetMemberDTO getMyProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        LevelService.LevelProgressDto levelProgress = levelService.calculateLevelProgress(member.getScore());
        String profileImageUrl = s3Service.generateProfileImageUrl(member.getProfileImageKey());

        return new MemberResponseDTO.GetMemberDTO(
                profileImageUrl,
                member.getNickname(),
                levelProgress.currentLevel(),
                levelProgress.remainingScore()
        );
    }

    @Override
    public List<RecipeResponseDTO.RecipePreviewDTO> getRecentlyViewedRecipes(Long memberId) {
        // Redis에서 레시피 ID 목록 조회
        List<Long> recipeIds = recentlyViewedRecipeManager.getList(memberId);

        if (recipeIds.isEmpty()) {
            return Collections.emptyList();
        }

        // DB에서 레시피 정보 조회 (순서 유지)
        Map<Long, Recipe> recipeMap = recipeRepository.findAllById(recipeIds)
                .stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));

        Set<Long> bookmarkedRecipeIds = memberRecipeRepository
                .findBookmarkedRecipeIdsByMemberId(memberId, recipeIds);


        // Redis에서 조회한 순서대로 반환
        return recipeIds.stream()
                .map(recipeId -> {
                    Recipe recipe = recipeMap.get(recipeId);
                    if (recipe == null) {
                        return null; // 삭제된 레시피는 제외
                    }
                    Boolean isBookmarked = bookmarkedRecipeIds.contains(recipeId);
                    return RecipeConverter.toRecipePreviewDTO(recipe, isBookmarked);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

