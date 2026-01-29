package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeDetailResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.RecipeStep;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class RecipeDetailConverter {

    private static final int MAX_STEPS = 10;

    public RecipeDetailResponseDTO toDTO(
            Recipe recipe,
            List<RecipeIngredient> recipeIngredients,
            Set<Long> memberIngredientIds,
            boolean summaryExists,
            List<RecipeStep> steps,
            boolean isBookmarked
    ) {
        List<RecipeDetailResponseDTO.RecipeStepResponse> stepResponses =
                convertSteps(steps);

        List<RecipeDetailResponseDTO.IngredientResponse> ingredientResponses =
                convertIngredients(recipeIngredients, memberIngredientIds);

        return RecipeDetailResponseDTO.builder()
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .imageUrl(recipe.getImageUrl())
                .videoUrl(recipe.getVideoUrl())
                .videoId(recipe.getVideoId())
                .channel(recipe.getChannel())
                .channelId(recipe.getChannelId())
                .channelProfileImageUrl(recipe.getChannelProfileImageUrl())
                .cookingTimeMinutes(recipe.getCookingTime())
                .videoDuration(recipe.getTime())
                .views(recipe.getViews())
                .difficulty(recipe.getLevel())
                .ratingCount(recipe.getRatingCount())
                .ingredients(ingredientResponses)
                .summaryExists(summaryExists)
                .steps(stepResponses)
                .isBookmarked(isBookmarked)
                .build();
    }

    private List<RecipeDetailResponseDTO.RecipeStepResponse> convertSteps(List<RecipeStep> steps) {
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }

        List<RecipeDetailResponseDTO.RecipeStepResponse> stepResponses = steps.stream()
                .map(this::convertStep)
                .toList();

        return limitSteps(stepResponses);
    }

    private RecipeDetailResponseDTO.RecipeStepResponse convertStep(RecipeStep step) {
        return RecipeDetailResponseDTO.RecipeStepResponse.builder()
                .stepNumber(step.getStepNumber())
                .title(step.getTitle())
                .description(step.getDescription())
                .videoTime(RecipeDetailResponseDTO.RecipeStepResponse
                        .formatVideoTime(step.getVideoTime()))
                .build();
    }

    private List<RecipeDetailResponseDTO.RecipeStepResponse> limitSteps(
            List<RecipeDetailResponseDTO.RecipeStepResponse> steps
    ) {
        if (steps.size() > MAX_STEPS) {
            return steps.subList(0, MAX_STEPS);
        }
        return steps;
    }

    private List<RecipeDetailResponseDTO.IngredientResponse> convertIngredients(
            List<RecipeIngredient> recipeIngredients,
            Set<Long> memberIngredientIds
    ) {
        return recipeIngredients.stream()
                .map(ri -> convertIngredient(ri, memberIngredientIds))
                .toList();
    }

    private RecipeDetailResponseDTO.IngredientResponse convertIngredient(
            RecipeIngredient recipeIngredient,
            Set<Long> memberIngredientIds
    ) {
        return RecipeDetailResponseDTO.IngredientResponse.builder()
                .ingredientId(recipeIngredient.getIngredient().getId())
                .name(recipeIngredient.getIngredient().getName())
                .amount(recipeIngredient.getAmount())
                .unit(recipeIngredient.getIngredient().getUnit() != null
                        ? recipeIngredient.getIngredient().getUnit().getLabel()
                        : null)
                .hasIngredient(memberIngredientIds.contains(
                        recipeIngredient.getIngredient().getId()))
                .build();
    }
}