package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.crawler.RecipeCrawler;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.GeminiResponseConverter;

import java.util.List;

public interface RecipeCommandService {

    // --- Public API ---
    RecipeResponseDTO.CookingRecordCreateResponseDTO createCookingRecord(Long memberId, Long recipeId, int rating);
    RecipeResponseDTO.BookmarkToggleResult toggleBookmark(Long memberId, Long recipeId);

    // --- For Facade (DB Transactions Only) ---
    List<String> getAllIngredientNames();
    
    Long saveRecipeAndIngredients(Long dishId, String videoId, RecipeCrawler.RecipeData data);
    
    Recipe getRecipeForSummary(Long recipeId);
    
    Summary tryCreateSummary(Long recipeId);
    
    RecipeResponseDTO.SummaryCreateResult getExistingSummaryResult(Long recipeId);
    
    RecipeResponseDTO.SummaryCreateResult saveSummarySteps(Long summaryId, List<GeminiResponseConverter.StepDraft> steps);
    
    void deleteSummary(Long summaryId);
}