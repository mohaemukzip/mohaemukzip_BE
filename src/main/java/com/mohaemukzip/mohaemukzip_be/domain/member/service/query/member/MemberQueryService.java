package com.mohaemukzip.mohaemukzip_be.domain.member.service.query.member;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;

import java.util.List;

public interface MemberQueryService {
    MemberResponseDTO.MyPageDTO getMyPage(Long memberId);
    List<RecipeResponseDTO.RecipePreviewDTO> getRecentlyViewedRecipes(Long memberId);
    RecipeResponseDTO.RecipePreviewListDTO getBookmarkedRecipes(Long memberId, int page);
}
