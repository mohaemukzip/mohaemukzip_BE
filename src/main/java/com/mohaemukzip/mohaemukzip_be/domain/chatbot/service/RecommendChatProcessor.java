package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatRoom;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.MemberCookHistory;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberCookHistoryRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
public class RecommendChatProcessor implements ChatProcessor {

    private final MemberIngredientRepository memberIngredientRepository;
    private final MemberCookHistoryRepository memberCookHistoryRepository;
    private final RecipeRepository recipeRepository;

    @Override
    public String analyzeIntent(String userMessage) {
        if (userMessage.contains("추천") || userMessage.contains("뭐 먹지") || userMessage.contains("냉장고")) {
            return "RECOMMENDATION";
        }
        return "GENERAL";
    }

    @Override
    public ChatProcessorResult process(ChatRoom chatRoom, String userMessage, String intent) {
        if (!"RECOMMENDATION".equals(intent)) {
            return ChatProcessorResult.builder()
                    .message("안녕하세요! 무엇을 도와드릴까요? '냉장고 파먹기'나 '메뉴 추천'이라고 말씀해 주시면 도와드릴게요.")
                    .recipes(Collections.emptyList())
                    .build();
        }

        Long memberId = chatRoom.getMemberId();

        // Step 1. 사용자 냉장고 스캔 (유통기한 임박순)
        List<MemberIngredient> myIngredients = memberIngredientRepository.findAllByMemberIdOrderByExpireDateAsc(memberId);
        
        if (myIngredients.isEmpty()) {
            List<Recipe> randomRecipes = recipeRepository.findAll().stream().limit(5).collect(Collectors.toList());
            return ChatProcessorResult.builder()
                    .message("냉장고가 비어있네요. 요즘 인기 있는 레시피를 추천해 드릴게요.")
                    .recipes(randomRecipes)
                    .build();
        }

        // Step 2. 추천 후보군 선정 (재료 이름으로 레시피 제목 검색)
        List<String> urgentIngredientNames = myIngredients.stream()
                .limit(3)
                .map(mi -> mi.getIngredient().getName())
                .collect(Collectors.toList());

        List<Recipe> candidateRecipes = new ArrayList<>();
        for (String ingredientName : urgentIngredientNames) {
            List<Recipe> recipes = recipeRepository.findByTitleContaining(ingredientName);
            candidateRecipes.addAll(recipes);
        }

        // 검색된 레시피가 없으면 인기 레시피 추천
        if (candidateRecipes.isEmpty()) {
             List<Recipe> randomRecipes = recipeRepository.findAll().stream().limit(5).collect(Collectors.toList());
             return ChatProcessorResult.builder()
                    .message("가진 재료로 만들만한 레시피를 찾지 못했어요. 대신 인기 레시피는 어떠세요?")
                    .recipes(randomRecipes)
                    .build();
        }

        // Step 3. 중복 추천 필터링 (최근 7일 이내 요리한 레시피 제외)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<MemberCookHistory> histories = memberCookHistoryRepository.findAllByMemberIdAndCookedAtAfter(memberId, sevenDaysAgo);
        
        Set<Long> cookedRecipeIds = histories.stream()
                .map(history -> history.getRecipe().getId())
                .collect(Collectors.toSet());

        List<Recipe> finalRecipes = candidateRecipes.stream()
                .filter(recipe -> !cookedRecipeIds.contains(recipe.getId()))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
        
        // 최종 메시지 생성
        String mainIngredient = urgentIngredientNames.get(0);
        String message = String.format("유통기한이 임박한 '%s' 등을 활용할 수 있는 레시피를 찾아봤어요!", mainIngredient);

        return ChatProcessorResult.builder()
                .message(message)
                .recipes(finalRecipes)
                .build();
    }
}
