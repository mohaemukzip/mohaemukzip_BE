package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatResultDto {
        private Long id;
        private SenderType senderType;
        private String message;
        private LocalDateTime createdAt;
        private String formattedTime; // 화면 표시용 시간 (예: 오후 2:30)
        
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<RecipeCardDto> recommendRecipes;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipeCardDto {
        private Long recipeId;
        private String title;
        private String imageUrl;
    }
}
