package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatRoom;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class BasicChatProcessor implements ChatProcessor {

    private final RecipeRepository recipeRepository;

    @Override
    public String analyzeIntent(String userMessage) {
        if (userMessage.contains("추천") || userMessage.contains("뭐 먹지")) {
            return "RECOMMENDATION";
        } else if (userMessage.contains("다이어트")) {
            return "DIET";
        }
        return "GENERAL";
    }

    @Override
    public ChatProcessorResult process(ChatRoom chatRoom, String userMessage, String intent) {
        // BasicChatProcessor는 단순 에코 또는 기본 응답만 수행
        
        String message = "안녕하세요! 기본 챗봇입니다. (RecommendChatProcessor가 활성화되어야 합니다)";
        
        return ChatProcessorResult.builder()
                .message(message)
                .recipes(Collections.emptyList())
                .build();
    }
}
