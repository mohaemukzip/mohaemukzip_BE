package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.processor;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.external.GeminiService;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.CookingRecord;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.CookingRecordRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class RecommendChatProcessor implements ChatProcessor {

    private final MemberIngredientRepository memberIngredientRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final RecipeRepository recipeRepository;
    private final GeminiService geminiService;

    private static final String GENERAL_SYSTEM_PROMPT = 
            "너는 자취생을 위한 다정한 요리 도우미 '요선생'이야. " +
            "친절하고, 이모티콘을 적절히 사용하며, 3문장 이내로 간결하게 답변해. " +
            "요리나 식재료와 관련 없는 질문(정치, 코딩, 연애 등)에는 '저는 요리 이야기만 할 수 있어요 🍳'라고 정중히 거절해. " +
            "**답변 형식: 맨 첫 줄에 핵심 내용을 요약한 '제목'을 적고, `|||` (파이프 3개) 문자열로 구분한 뒤 본문을 작성해. (예: 돈까스 요리 꿀팁! ||| 돼지고기 등심은...)**";

    private static final String RECOMMEND_SYSTEM_PROMPT = 
            GENERAL_SYSTEM_PROMPT + 
            " **중요: 반드시 아래 제공된 [추천 후보 리스트] 중에서 사용자의 질문 의도와 상황에 가장 잘 맞는 메뉴를 하나 골라 추천해야 해. 리스트에 없는 요리는 절대 언급하지 마.**";

    @Override
    public String analyzeIntent(String userMessage) {
        if (userMessage.contains("추천") || userMessage.contains("뭐 먹지") || userMessage.contains("냉장고") || 
            userMessage.contains("배고파") || userMessage.contains("메뉴") || userMessage.contains("요리") ||
            userMessage.contains("먹고 싶어") || userMessage.contains("먹을래") || userMessage.contains("땡겨") ||
            userMessage.contains("당겨") || userMessage.contains("해줘") || userMessage.contains("할까")) {
            return "RECOMMENDATION";
        }
        return "GENERAL";
    }

    @Override
    public ChatProcessorResult process(Long memberId, String userMessage, String intent) {
        try {
            log.info("ChatProcessor 처리 시작 - Intent: {}, UserMessage Length: {}", intent, (userMessage != null ? userMessage.length() : 0));

            if (!"RECOMMENDATION".equals(intent)) {
                String aiResponse = geminiService.generateChatResponse(GENERAL_SYSTEM_PROMPT, userMessage);
                return parseResponse(aiResponse, "요선생의 답변", Collections.emptyList());
            }

            Set<Recipe> candidateSet = new HashSet<>();

            String[] keywords = userMessage.split("\\s+");
            for (String keyword : keywords) {
                if (keyword.length() > 1) {
                    candidateSet.addAll(recipeRepository.findByTitleContaining(keyword));
                }
            }

            List<MemberIngredient> myIngredients = memberIngredientRepository.findAllByMemberIdOrderByExpireDateAsc(memberId);
            List<String> urgentIngredientNames = new ArrayList<>();

            if (!myIngredients.isEmpty()) {
                urgentIngredientNames = myIngredients.stream()
                        .limit(3)
                        .map(mi -> mi.getIngredient().getName())
                        .collect(Collectors.toList());

                for (String ingredientName : urgentIngredientNames) {
                    candidateSet.addAll(recipeRepository.findByTitleContaining(ingredientName));
                }
            }

            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<CookingRecord> histories = cookingRecordRepository.findAllByMemberIdAndCreatedAtAfter(memberId, sevenDaysAgo);
            Set<Long> cookedRecipeIds = histories.stream().map(h -> h.getRecipe().getId()).collect(Collectors.toSet());

            List<Recipe> filteredRecipes = candidateSet.stream()
                    .filter(r -> !cookedRecipeIds.contains(r.getId()))
                    .collect(Collectors.toList());

            int neededCount = 5 - filteredRecipes.size();
            if (neededCount > 0) {
                List<Recipe> randomRecipes = recipeRepository.findRandomRecipes(neededCount);
                
                for (Recipe r : randomRecipes) {
                    if (!cookedRecipeIds.contains(r.getId()) && !filteredRecipes.contains(r)) {
                        filteredRecipes.add(r);
                    }
                }
            }

            List<Recipe> finalRecipes = filteredRecipes.stream().limit(5).collect(Collectors.toList());

            String userPrompt = buildUserPrompt(userMessage, urgentIngredientNames, finalRecipes);
            String aiResponse = geminiService.generateChatResponse(RECOMMEND_SYSTEM_PROMPT, userPrompt);

            return parseResponse(aiResponse, "오늘의 추천 메뉴", finalRecipes);

        } catch (Exception e) {
            log.error("ChatProcessor 처리 중 예외 발생", e);
            return ChatProcessorResult.builder()
                    .title("일시적 오류")
                    .message("죄송해요, 처리 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
                    .recipes(Collections.emptyList())
                    .build();
        }
    }

    private ChatProcessorResult parseResponse(String aiResponse, String defaultTitle, List<Recipe> recipes) {
        String title = defaultTitle;
        String message = aiResponse;

        if (aiResponse == null) {
            log.warn("AI 응답 실패 -> Fallback 메시지 반환");
            if (!recipes.isEmpty()) {
                String mainRecipe = recipes.get(0).getTitle();
                message = String.format("죄송해요, 잠시 연결이 불안정해요 😢 하지만 지금 상황에 딱 맞는 **%s** 레시피를 찾아왔어요!", mainRecipe);
            } else {
                message = "죄송해요, 잠시 연결이 불안정해요 😢 '냉장고 파먹기'나 '메뉴 추천'이라고 말씀해 주시면 레시피를 찾아드릴게요!";
            }
        } else {
            String[] parts = aiResponse.split("\\|\\|\\|");
            if (parts.length >= 2) {
                title = parts[0].trim();
                message = parts[1].trim();
            }
        }

        return ChatProcessorResult.builder()
                .title(title)
                .message(message)
                .recipes(recipes)
                .build();
    }

    private String buildUserPrompt(String userMessage, List<String> ingredients, List<Recipe> recipes) {
        String ingredientStr = ingredients.isEmpty() ? "없음" : String.join(", ", ingredients);
        String recipeStr = recipes.stream().map(Recipe::getTitle).collect(Collectors.joining(", "));

        return String.format(
                "[사용자 정보]\n" +
                "- 임박한 재료: [%s]\n" +
                "- 추천 후보 리스트: [%s]\n\n" +
                "[사용자 질문]\n" +
                "\"%s\"\n\n" +
                "[요청]\n" +
                "위 [추천 후보 리스트] 중에서 사용자의 질문 의도(키워드)와 냉장고 재료 상황을 고려하여 가장 적절한 메뉴 하나를 골라 추천해줘. " +
                "이유도 간단히 설명해줘. (예: '냉장고에 있는 두부를 활용할 수 있어요', '말씀하신 파스타 요리예요')",
                ingredientStr, recipeStr, userMessage
        );
    }
}
