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
    private final OpenAiService openAiService;

    private static final String SYSTEM_PROMPT = 
            "너는 자취생을 위한 다정한 요리 도우미 '요선생'이야. " +
            "친절하고, 이모티콘을 적절히 사용하며, 3문장 이내로 간결하게 답변해. " +
            "요리나 식재료와 관련 없는 질문(정치, 코딩, 연애 등)에는 '저는 요리 이야기만 할 수 있어요 🍳'라고 정중히 거절해.";

    @Override
    public String analyzeIntent(String userMessage) {
        if (userMessage.contains("추천") || userMessage.contains("뭐 먹지") || userMessage.contains("냉장고")) {
            return "RECOMMENDATION";
        }
        return "GENERAL";
    }

    @Override
    public ChatProcessorResult process(ChatRoom chatRoom, String userMessage, String intent) {
        // 1. 일반 대화 처리 (요리 관련 질문 등)
        if (!"RECOMMENDATION".equals(intent)) {
            String aiResponse = openAiService.generateChatResponse(SYSTEM_PROMPT, userMessage);
            
            // Fallback: 일반 대화에서 AI 실패 시
            if (aiResponse == null) {
                aiResponse = "죄송해요, 잠시 연결이 불안정해요 😢 '냉장고 파먹기'나 '메뉴 추천'이라고 말씀해 주시면 레시피를 찾아드릴게요!";
            }

            return ChatProcessorResult.builder()
                    .message(aiResponse)
                    .recipes(Collections.emptyList())
                    .build();
        }

        Long memberId = chatRoom.getMemberId();

        // Step 1. 사용자 냉장고 스캔
        List<MemberIngredient> myIngredients = memberIngredientRepository.findAllByMemberIdOrderByExpireDateAsc(memberId);
        
        // Step 2. 추천 후보군 선정
        List<Recipe> candidateRecipes = new ArrayList<>();
        List<String> urgentIngredientNames = new ArrayList<>();

        if (!myIngredients.isEmpty()) {
            urgentIngredientNames = myIngredients.stream()
                    .limit(3)
                    .map(mi -> mi.getIngredient().getName())
                    .collect(Collectors.toList());

            for (String ingredientName : urgentIngredientNames) {
                candidateRecipes.addAll(recipeRepository.findByTitleContaining(ingredientName));
            }
        }

        if (candidateRecipes.isEmpty()) {
             candidateRecipes = recipeRepository.findAll().stream().limit(5).collect(Collectors.toList());
        }

        // Step 3. 중복 추천 필터링
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<MemberCookHistory> histories = memberCookHistoryRepository.findAllByMemberIdAndCookedAtAfter(memberId, sevenDaysAgo);
        Set<Long> cookedRecipeIds = histories.stream().map(h -> h.getRecipe().getId()).collect(Collectors.toSet());

        List<Recipe> finalRecipes = candidateRecipes.stream()
                .filter(r -> !cookedRecipeIds.contains(r.getId()))
                .distinct()
                .limit(5)
                .collect(Collectors.toList());

        // Step 4. AI 멘트 생성 (RAG)
        String userPrompt = buildUserPrompt(userMessage, urgentIngredientNames, finalRecipes);
        String aiResponse = openAiService.generateChatResponse(SYSTEM_PROMPT, userPrompt);

        // Fallback: 추천 로직에서 AI 실패 시 (DB 데이터 활용)
        if (aiResponse == null) {
            String mainIngredient = urgentIngredientNames.isEmpty() ? "재료" : urgentIngredientNames.get(0);
            String mainRecipe = finalRecipes.isEmpty() ? "인기 요리" : finalRecipes.get(0).getTitle();
            
            aiResponse = String.format(
                "죄송해요, 잠시 요선생의 연결이 불안정해요 😢 하지만 유통기한이 임박한 **%s** 등으로 만들 수 있는 **%s** 레시피를 찾아왔어요!", 
                mainIngredient, mainRecipe
            );
        }

        return ChatProcessorResult.builder()
                .message(aiResponse)
                .recipes(finalRecipes)
                .build();
    }

    private String buildUserPrompt(String userMessage, List<String> ingredients, List<Recipe> recipes) {
        String ingredientStr = ingredients.isEmpty() ? "없음" : String.join(", ", ingredients);
        String recipeStr = recipes.stream().map(Recipe::getTitle).collect(Collectors.joining(", "));

        return String.format(
                "[사용자 정보]\n" +
                "- 임박한 재료: [%s]\n" +
                "- 추천 레시피 후보: [%s]\n\n" +
                "[사용자 질문]\n" +
                "\"%s\"\n\n" +
                "[요청]\n" +
                "위 정보를 바탕으로 사용자에게 자연스럽게 추천 멘트를 작성해줘. " +
                "레시피 목록을 나열하지 말고, '이런 요리는 어떠세요?' 식으로 제안해줘.",
                ingredientStr, recipeStr, userMessage
        );
    }
}
