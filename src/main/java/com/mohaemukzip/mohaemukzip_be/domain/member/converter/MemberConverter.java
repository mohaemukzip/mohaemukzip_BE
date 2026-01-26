package com.mohaemukzip.mohaemukzip_be.domain.member.converter;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.MemberRecipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.MemberRecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.global.s3.S3Service;
import com.mohaemukzip.mohaemukzip_be.global.service.LevelService;
import com.mohaemukzip.mohaemukzip_be.global.service.RecentlyViewedRecipeManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberConverter {
    private final LevelService levelService;
    private final S3Service s3Service;

    public MemberResponseDTO.MyPageDTO toMyPageDTO(
            Member member,
            List<RecipeResponseDTO.RecipePreviewDTO> recentlyViewed,
            List<RecipeResponseDTO.RecipePreviewDTO> bookmarked
    ) {
        LevelService.LevelProgressDto levelProgress = levelService.calculateLevelProgress(member.getScore());
        String profileImageUrl = s3Service.generateProfileImageUrl(member.getProfileImageKey());

        return new MemberResponseDTO.MyPageDTO(
                profileImageUrl,
                member.getNickname(),
                levelProgress.currentLevel(),
                levelProgress.remainingScore(),
                convertToMemberRecipePreviewDTO(recentlyViewed),
                convertToMemberRecipePreviewDTO(bookmarked)
        );
    }

    private List<MemberResponseDTO.RecipePreviewDTO> convertToMemberRecipePreviewDTO(
            List<RecipeResponseDTO.RecipePreviewDTO> recipeList
    ) {
        return recipeList.stream()
                .map(recipe -> new MemberResponseDTO.RecipePreviewDTO(
                        recipe.getId(),
                        recipe.getVideoId(),
                        recipe.getVideoDuration(),
                        recipe.getIsBookmarked()
                ))
                .toList();
    }
}