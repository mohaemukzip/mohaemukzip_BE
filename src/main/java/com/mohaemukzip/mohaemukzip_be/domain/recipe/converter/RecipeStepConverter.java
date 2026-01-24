package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.RecipeStep;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecipeStepConverter {

    public List<RecipeStep> toEntities(Summary summary, List<GeminiResponseConverter.StepDraft> stepDrafts) {
        return stepDrafts.stream()
                .map(draft -> toEntity(summary, draft))
                .toList();
    }

    public RecipeStep toEntity(Summary summary, GeminiResponseConverter.StepDraft draft) {
        return RecipeStep.builder()
                .summary(summary)
                .stepNumber(draft.stepNumber())
                .title(draft.title())
                .description(draft.description())
                .videoTime(draft.videoTime())
                .build();
    }
}