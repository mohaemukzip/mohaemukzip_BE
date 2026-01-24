package com.mohaemukzip.mohaemukzip_be.domain.chatbot.converter;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatRecipeResponse;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatResponse;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatMessage;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatRoom;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.ChatState;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ChatConverter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA);

    public static ChatRoom toChatRoom(Long memberId) {
        return ChatRoom.builder()
                .memberId(memberId)
                .state(ChatState.ING)
                .build();
    }

    public static ChatMessage toChatMessage(ChatRoom chatRoom, SenderType senderType, String message) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderType(senderType)
                .message(message)
                .build();
    }

    public static ChatResponse toChatResponse(ChatMessage chatMessage) {
        return ChatResponse.builder()
                .id(chatMessage.getId())
                .senderType(chatMessage.getSenderType())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .formattedTime(formatTime(chatMessage.getCreatedAt()))
                .build();
    }

    public static ChatResponse toChatResponse(ChatMessage chatMessage, List<Recipe> recipes) {
        List<ChatRecipeResponse> recipeCards = recipes.stream()
                .map(ChatConverter::toChatRecipeResponse)
                .collect(Collectors.toList());

        return ChatResponse.builder()
                .id(chatMessage.getId())
                .senderType(chatMessage.getSenderType())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .formattedTime(formatTime(chatMessage.getCreatedAt()))
                .recommendRecipes(recipeCards)
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
