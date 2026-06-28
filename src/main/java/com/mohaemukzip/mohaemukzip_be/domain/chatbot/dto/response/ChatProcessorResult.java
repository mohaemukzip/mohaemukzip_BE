package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatProcessorResult {
    private String title;
    private String message;
    /** 기존 레시피 엔티티 목록 (RecommendChatProcessor 하위 호환용) */
    private List<Recipe> recipes;
    /** RAG 방식으로 생성된 레시피 카드 목록 (RagChatProcessor 사용) */
    private List<RecipeCardResponse> recipeCards;
}

