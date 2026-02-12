package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.processor;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatProcessorResult;

public interface ChatProcessor {
    String analyzeIntent(String userMessage);
    // ChatRoom 엔티티 제거 -> memberId로 식별
    ChatProcessorResult process(Long memberId, String userMessage, String intent);
}
