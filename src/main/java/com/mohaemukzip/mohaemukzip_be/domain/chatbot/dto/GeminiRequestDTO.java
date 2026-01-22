package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GeminiRequestDTO {
    
    private SystemInstruction systemInstruction;
    private List<Content> contents;

    @Getter
    @Builder
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Getter
    @Builder
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @Builder
    public static class Part {
        private String text;
    }
}
