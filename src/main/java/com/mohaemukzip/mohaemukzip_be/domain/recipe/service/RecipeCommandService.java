package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

public interface RecipeCommandService {

    void rateRecipe(Long memberId, Long recipeId, int rating);

    Long saveRecipeByVideoId(String videoId);

    RecipeCommandServiceImpl.SummaryCreateResult createSummary(Long recipeId);


}
