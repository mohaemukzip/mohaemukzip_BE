package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatProcessorResult {
    private String message;
    private List<Recipe> recipes;
}
