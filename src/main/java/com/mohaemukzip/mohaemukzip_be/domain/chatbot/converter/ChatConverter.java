package com.mohaemukzip.mohaemukzip_be.domain.chatbot.converter;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatMessage;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatRoom;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.ChatState;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;

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

    public static ChatResponseDTO.ChatResultDto toChatResultDto(ChatMessage chatMessage) {
        return ChatResponseDTO.ChatResultDto.builder()
                .id(chatMessage.getId())
                .senderType(chatMessage.getSenderType())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .formattedTime(chatMessage.getCreatedAt().format(TIME_FORMATTER))
                .build();
    }

    public static ChatResponseDTO.ChatResultDto toChatResultDto(ChatMessage chatMessage, List<Recipe> recipes) {
        List<ChatResponseDTO.RecipeCardDto> recipeCards = recipes.stream()
                .map(ChatConverter::toRecipeCardDto)
                .collect(Collectors.toList());

        return ChatResponseDTO.ChatResultDto.builder()
                .id(chatMessage.getId())
                .senderType(chatMessage.getSenderType())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .formattedTime(chatMessage.getCreatedAt().format(TIME_FORMATTER))
                .recommendRecipes(recipeCards)
                .build();
    }

    public static ChatResponseDTO.RecipeCardDto toRecipeCardDto(Recipe recipe) {
        return ChatResponseDTO.RecipeCardDto.builder()
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .imageUrl(recipe.getImageUrl())
                .build();
    }
}
