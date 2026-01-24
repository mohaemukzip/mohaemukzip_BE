package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.RecipeIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.RecipeIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.RecipeConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeDetailResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.MemberRecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeCategoryRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.SummaryRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeQueryServiceImpl implements RecipeQueryService {

    private final RecipeCategoryRepository recipeCategoryRepository;
    private final MemberRecipeRepository memberRecipeRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final SummaryRepository summaryRepository;
    private static final int PAGE_SIZE = 10;
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final SummaryRepository summaryRepository;
    private final MemberIngredientRepository memberIngredientRepository;
    private final RecipeStepRepository recipeStepRepository;
    private final MemberRecipeRepository memberRecipeRepository;


    @Override
    public RecipeResponseDTO.RecipePreviewListDTO getRecipesByCategoryId(Long categoryId, Integer page, Member member) {
        Page<Recipe> recipePage = recipeCategoryRepository.findRecipesByCategoryId(categoryId, PageRequest.of(page, PAGE_SIZE));

        // 첫 페이지인데 데이터가 없다면 -> 존재하지 않는 카테고리로 간주
        if (page == 0 && recipePage.isEmpty()) {
            throw new BusinessException(ErrorStatus.CATEGORY_NOT_FOUND);
        }

        Set<Long> bookmarkedRecipeIds = Collections.emptySet();
        if (member != null && !recipePage.isEmpty()) {
            List<Long> recipeIds = recipePage.getContent().stream()
                    .map(Recipe::getId)
                    .collect(Collectors.toList());
            bookmarkedRecipeIds = memberRecipeRepository
                    .findBookmarkedRecipeIds(member, recipeIds);
        }

        return RecipeConverter.toRecipePreviewListDTO(recipePage, bookmarkedRecipeIds);
    }

    @Override
    public RecipeResponseDTO.RecipeDetailDTO getRecipeDetail(Long recipeId, Member member) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.RECIPE_NOT_FOUND));

        boolean isBookmarked = false;
        if (member != null) {
            Set<Long> bookmarkedIds = memberRecipeRepository.findBookmarkedRecipeIds(member, List.of(recipe.getId()));
            isBookmarked = bookmarkedIds.contains(recipe.getId());
        }

        List<String> ingredients = recipeIngredientRepository.findIngredientNamesByRecipeId(recipeId);
        
        // Summary 조회 시 fetch join을 사용하면 좋지만, 현재 SummaryRepository.findByRecipeId는 
        // 단건 조회이므로 N+1 문제가 발생하지 않음. 
        // 다만, Summary 엔티티가 Recipe와 1:1 관계이고 Lazy Loading 설정이 되어 있다면 
        // Recipe 조회 시 Summary를 함께 가져오는 것이 성능상 유리할 수 있음.
        // 현재 구조에서는 별도 조회로 유지하되, 향후 트래픽 증가 시 최적화 포인트로 남겨둠.
        List<String> instructions = summaryRepository.findByRecipeId(recipeId)
                .map(Summary::getDescription)
                .map(desc -> List.of(desc.split("\n"))) 
                .orElse(Collections.emptyList());

        return RecipeConverter.toRecipeDetailDTO(recipe, isBookmarked, ingredients, instructions);
    }

    @Override
    public RecipeDetailResponseDTO getRecipeDetail(Long recipeId, Long memberId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.RECIPE_NOT_FOUND));

        boolean isBookmarked =
                memberRecipeRepository.existsByMember_IdAndRecipe_Id(memberId, recipeId);

        List<RecipeIngredient> recipeIngredients =
                recipeIngredientRepository.findAllByRecipeId(recipeId);

        // recipe에 포함된 ingredientId 목록 추출
        List<Long> ingredientIds = recipeIngredients.stream()
                .map(ri -> ri.getIngredient().getId())
                .toList();

        // 유저가 보유한 재료 id Set 조회 (N+1 방지)
        Set<Long> memberIngredientIds =
                memberIngredientRepository.findIngredientIdsByMemberIdAndIngredientIdIn(
                        memberId,
                        ingredientIds
                );

        boolean summaryExists = summaryRepository.existsByRecipeId(recipeId);

        // summary 조회
        Summary summary = summaryRepository.findByRecipeId(recipeId).orElse(null);

        List<RecipeDetailResponseDTO.RecipeStepResponse> steps = List.of();


        if (summary != null) {
            steps = recipeStepRepository
                    .findAllBySummaryIdOrderByStepNumberAsc(summary.getId())
                    .stream()
                    .map(step -> RecipeDetailResponseDTO.RecipeStepResponse.builder()
                            .stepNumber(step.getStepNumber())
                            .title(step.getTitle())
                            .description(step.getDescription())
                            .videoTime(RecipeDetailResponseDTO.RecipeStepResponse
                                    .formatVideoTime(step.getVideoTime()))
                            .build()
                    )
                    .toList();
        }

        if (steps.size() > 10) {
            steps = steps.subList(0, 10);
        }

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
                .ingredients(
                        recipeIngredients.stream()
                                .map(ri -> RecipeDetailResponseDTO.IngredientResponse.builder()
                                        .ingredientId(ri.getIngredient().getId())
                                        .name(ri.getIngredient().getName())
                                        .amount(ri.getAmount())
                                        .unit(
                                                ri.getIngredient().getUnit() != null
                                                        ? ri.getIngredient().getUnit().name()
                                                        : null
                                        )
                                        .hasIngredient(
                                                memberIngredientIds.contains(
                                                        ri.getIngredient().getId()
                                                )
                                        )
                                        .build()
                                )
                                .toList()
                )
                .summaryExists(summaryExists)
                .steps(steps)
                .isBookmarked(isBookmarked)
                .build();
    }

}
