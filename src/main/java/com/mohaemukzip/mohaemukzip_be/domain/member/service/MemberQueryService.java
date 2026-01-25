package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;

import java.util.List;

public interface MemberQueryService {
    MemberResponseDTO.GetMemberDTO getMyProfile(Long memberId);
    List<RecipeResponseDTO.RecipePreviewDTO> getRecentlyViewedRecipes(Long memberId);
}
