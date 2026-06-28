package com.mohaemukzip.mohaemukzip_be.domain.chatbot.converter;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatRecipeResponse;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatResponse;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ChatConverter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA);

    public static ChatResponse toChatResponse(ChatProcessorResult result, String id) {
        // 기존 방식: Recipe 엔티티 → ChatRecipeResponse 변환
        List<ChatRecipeResponse> legacyRecipes = null;
        if (result.getRecipes() != null && !result.getRecipes().isEmpty()) {
            legacyRecipes = result.getRecipes().stream()
                    .map(ChatConverter::toChatRecipeResponse)
                    .collect(Collectors.toList());
        }

        LocalDateTime now = LocalDateTime.now();
        return ChatResponse.builder()
                .id(id)
                .senderType(SenderType.BOT)
                .title(result.getTitle())
                .message(result.getMessage())
                .createdAt(now)
                .formattedTime(formatTime(now))
                .recommendRecipes(legacyRecipes)
                .recipeCards(result.getRecipeCards()) // RAG 방식 카드 (null이면 JSON에서 제외됨)
                .build();
    }

    public static ChatRecipeResponse toChatRecipeResponse(Recipe recipe) {
        return ChatRecipeResponse.builder()
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .imageUrl(recipe.getImageUrl())
                .build();
    }

    private static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMATTER) : null;
    }
}
