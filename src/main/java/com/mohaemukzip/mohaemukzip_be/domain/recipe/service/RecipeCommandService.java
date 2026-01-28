package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;

public interface RecipeCommandService {

    Long saveRecipeByVideoId(String videoId);

    RecipeCommandServiceImpl.SummaryCreateResult createSummary(Long recipeId);

    RecipeResponseDTO.CookingRecordCreateResponseDTO createCookingRecord(
            Long memberId,
            Long recipeId,
            int rating
    );

    boolean toggleBookmark(Member member, Long recipeId);
}