package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OpenAiRequestDTO {
    private String model;
    private List<Message> messages;
    private int max_tokens;
    private double temperature;

    @Getter
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
