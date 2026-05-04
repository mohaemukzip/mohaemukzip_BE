package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.RedisChatMessage;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.request.GeminiRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.RecipeCardResponse;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.external.GeminiService;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.helper.ChatContextHelper;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeSearchResponseDto;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.CookingRecord;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.CookingRecordRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.query.RecipeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RAG(Retrieval-Augmented Generation) 기반 챗봇 프로세서.
 *
 * [동작 흐름]
 * 1. [Retrieval] RecipeSearchService를 통해 사용자 질문과 유사한 레시피 상위 3개를 벡터 검색으로 찾음
 * 2. [Context 수집] 사용자의 냉장고 재료(임박 순) + 최근 7일 식사 이력을 조회
 * 3. [Generation] 위 정보를 엄격한 System Prompt와 함께 Gemini에 전달
 *    → Gemini는 반드시 JSON 배열 형태로만 응답해야 함 (추천 이유, 재료 매칭률 포함)
 * 4. Gemini 응답 JSON을 파싱하여 RecipeCardResponse 리스트로 반환
 *
 * @Primary: 기존 RecommendChatProcessor를 대체함 (기존 코드 삭제 없이 교체)
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class RagChatProcessor implements ChatProcessor {

    private final RecipeSearchService recipeSearchService;
    private final MemberIngredientRepository memberIngredientRepository;
    private final CookingRecordRepository cookingRecordRepository;
    private final GeminiService geminiService;
    private final ChatContextHelper chatContextHelper;
    private final ObjectMapper objectMapper;

    /**
     * [System Prompt - 엄격 모드]
     * Gemini가 JSON 배열 이외의 텍스트를 절대 출력하지 않도록 강제합니다.
     * 마크다운 코드 블록(```json ... ```)도 붙이지 않도록 명시합니다.
     */
    private static final String SYSTEM_PROMPT =
            "당신은 요리 레시피 추천 전문 AI입니다.\n" +
            "당신의 유일한 역할은 주어진 레시피 후보와 사용자 상황을 분석하여 아래 JSON 배열 형식으로만 응답하는 것입니다.\n\n" +
            "[엄격한 규칙 - 반드시 준수]\n" +
            "1. 응답은 반드시 순수한 JSON 배열([ ... ])로만 시작하고 끝나야 합니다.\n" +
            "2. ```json 같은 마크다운 코드 블록, 설명 문구, 인사말은 절대 포함하지 마세요.\n" +
            "3. JSON 외의 어떤 텍스트도 앞뒤로 절대 붙이지 마세요.\n" +
            "4. ingredients_match_rate는 사용자의 냉장고 재료와 레시피 재료의 예상 매칭률(0~100 정수)입니다.\n" +
            "5. recommend_reason은 사용자 상황(냉장고 재료, 최근 식사 이력)을 구체적으로 언급하며 2~3문장으로 작성하세요.\n" +
            "6. 반드시 아래 JSON 구조를 정확히 따르세요:\n\n" +
            "[\n" +
            "  {\n" +
            "    \"recipe_id\": 숫자,\n" +
            "    \"title\": \"레시피 제목\",\n" +
            "    \"recommend_reason\": \"추천 이유 2~3문장\",\n" +
            "    \"ingredients_match_rate\": 0~100 사이 정수\n" +
            "  }\n" +
            "]";

    @Override
    public ChatProcessorResult process(Long memberId, String userMessage, List<RedisChatMessage> history) {
        try {
            log.info("[RAG 챗봇] 처리 시작 - memberId: {}, query: {}", memberId, userMessage);

            // ──────────────────────────────────────────
            // 1. [Retrieval] 벡터 검색으로 관련 레시피 TOP 3 조회
            // ──────────────────────────────────────────
            List<RecipeSearchResponseDto> topRecipes = recipeSearchService.searchTop3ByVector(userMessage);
            log.info("[RAG 챗봇] 벡터 검색 완료 - {}건 발견", topRecipes.size());

            // ──────────────────────────────────────────
            // 2. [Context] 냉장고 재료 조회 (유통기한 임박 순 상위 5개)
            // ──────────────────────────────────────────
            List<MemberIngredient> ingredients = memberIngredientRepository
                    .findAllByMemberIdOrderByExpireDateAsc(memberId);
            List<String> fridgeIngredients = ingredients.stream()
                    .limit(5)
                    .map(mi -> mi.getIngredient().getName())
                    .collect(Collectors.toList());

            // ──────────────────────────────────────────
            // 3. [Context] 최근 7일 식사 이력 조회
            // ──────────────────────────────────────────
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<CookingRecord> cookingHistory = cookingRecordRepository
                    .findAllByMemberIdAndCreatedAtAfter(memberId, sevenDaysAgo);
            Set<String> recentMeals = cookingHistory.stream()
                    .map(h -> h.getRecipe().getTitle())
                    .collect(Collectors.toSet());

            // ──────────────────────────────────────────
            // 4. [Generation] Gemini 프롬프트 조합 및 호출
            // ──────────────────────────────────────────
            // 이전 대화 기록 주입 (최근 12턴 제한)
            List<GeminiRequestDTO.Content> contents = chatContextHelper.buildHistoryContents(history, 12);

            // 현재 사용자 메시지 + 컨텍스트 조합
            String userPrompt = buildRagPrompt(userMessage, topRecipes, fridgeIngredients, recentMeals);
            contents.add(GeminiRequestDTO.Content.builder()
                    .role("user")
                    .parts(List.of(GeminiRequestDTO.Part.builder().text(userPrompt).build()))
                    .build());

            String aiResponse = geminiService.generateChatResponse(SYSTEM_PROMPT, contents);
            log.info("[RAG 챗봇] Gemini 응답 수신 완료");

            // ──────────────────────────────────────────
            // 5. JSON 응답 파싱 → RecipeCardResponse 리스트 변환
            // ──────────────────────────────────────────
            List<RecipeCardResponse> recipeCards = parseJsonResponse(aiResponse, topRecipes);

            return ChatProcessorResult.builder()
                    .title("맞춤 레시피 추천")
                    .message("회원님의 상황에 맞는 레시피를 찾아봤어요! 아래 카드를 확인해 보세요 🍳")
                    .recipeCards(recipeCards)
                    .build();

        } catch (Exception e) {
            log.error("[RAG 챗봇] 처리 중 예외 발생", e);
            return ChatProcessorResult.builder()
                    .title("일시적 오류")
                    .message("죄송해요, 처리 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
                    .recipeCards(Collections.emptyList())
                    .build();
        }
    }

    /**
     * Gemini에 전달할 최종 사용자 프롬프트를 조합합니다.
     * 검색된 레시피 정보, 냉장고 재료, 최근 식사 이력을 구체적인 컨텍스트로 제공합니다.
     */
    private String buildRagPrompt(String userMessage,
                                   List<RecipeSearchResponseDto> topRecipes,
                                   List<String> fridgeIngredients,
                                   Set<String> recentMeals) {
        StringBuilder sb = new StringBuilder();

        sb.append("[사용자 질문]\n\"").append(userMessage).append("\"\n\n");

        sb.append("[추천 레시피 후보 (벡터 검색 결과)]\n");
        for (RecipeSearchResponseDto recipe : topRecipes) {
            sb.append(String.format("- ID: %d | 제목: %s | 유사도: %.4f\n",
                    recipe.getId(), recipe.getTitle(), recipe.getSimilarity()));
        }

        sb.append("\n[사용자 냉장고 재료 (유통기한 임박 순)]\n");
        if (fridgeIngredients.isEmpty()) {
            sb.append("- 등록된 재료 없음\n");
        } else {
            fridgeIngredients.forEach(i -> sb.append("- ").append(i).append("\n"));
        }

        sb.append("\n[최근 7일 식사 이력]\n");
        if (recentMeals.isEmpty()) {
            sb.append("- 식사 이력 없음\n");
        } else {
            recentMeals.forEach(m -> sb.append("- ").append(m).append("\n"));
        }

        sb.append("\n위 정보를 바탕으로 추천 레시피 후보 중에서 사용자 상황에 가장 적합한 것들을 선택하여 ")
          .append("지정된 JSON 형식으로만 응답하세요.");

        return sb.toString();
    }

    /**
     * Gemini의 JSON 응답 문자열을 RecipeCardResponse 리스트로 파싱합니다.
     * 파싱 실패 시 검색 결과를 기반으로 Fallback 카드를 생성합니다.
     */
    private List<RecipeCardResponse> parseJsonResponse(String aiResponse,
                                                        List<RecipeSearchResponseDto> fallbackRecipes) {
        if (aiResponse == null || aiResponse.isBlank()) {
            log.warn("[RAG 챗봇] Gemini 응답이 null 또는 비어있음 → Fallback 적용");
            return buildFallbackCards(fallbackRecipes);
        }

        try {
            // Gemini가 혹시 ```json ... ``` 을 붙일 경우 제거
            String cleaned = aiResponse
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            return objectMapper.readValue(cleaned, new TypeReference<List<RecipeCardResponse>>() {});
        } catch (Exception e) {
            log.error("[RAG 챗봇] JSON 파싱 실패, Fallback 적용. 원본 응답: {}", aiResponse, e);
            return buildFallbackCards(fallbackRecipes);
        }
    }

    /**
     * Gemini 응답 파싱 실패 시, 벡터 검색 결과만으로 최소한의 카드를 만들어 반환합니다.
     */
    private List<RecipeCardResponse> buildFallbackCards(List<RecipeSearchResponseDto> recipes) {
        return recipes.stream()
                .map(r -> RecipeCardResponse.builder()
                        .recipeId(r.getId())
                        .title(r.getTitle())
                        .recommendReason("지금 찾고 계신 요리와 가장 유사한 레시피예요!")
                        .ingredientsMatchRate(0)
                        .build())
                .collect(Collectors.toList());
    }
}
