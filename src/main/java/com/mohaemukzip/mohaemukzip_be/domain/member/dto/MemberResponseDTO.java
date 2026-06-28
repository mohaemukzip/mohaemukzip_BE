package com.mohaemukzip.mohaemukzip_be.domain.member.dto;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.enums.LoginType;

import java.util.List;

public class MemberResponseDTO {
    public record MyPageDTO(
            String profileImageUrl,
            String nickname,
            Integer level,
            Integer remainingScore,

            List<RecipePreviewDTO> recentlyViewedRecipes,
            List<RecipePreviewDTO> bookmarkedRecipes
    ) { }

    public record RecipePreviewDTO(
            Long id,
            String videoId,
            String videoDuration,
            Boolean isBookmarked
    ) { }

    public record AccountSettingDTO(
            String email,
            LoginType loginType
    ) {}
}