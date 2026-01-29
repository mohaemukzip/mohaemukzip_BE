package com.mohaemukzip.mohaemukzip_be.domain.member.converter;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.global.s3.S3Service;
import com.mohaemukzip.mohaemukzip_be.global.service.LevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

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