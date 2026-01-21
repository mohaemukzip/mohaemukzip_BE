package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatRoom;

public interface ChatProcessor {
    String analyzeIntent(String userMessage);
    // userMessage 파라미터 추가: AI 연동 및 대화 맥락 파악을 위해 필요
    ChatProcessorResult process(ChatRoom chatRoom, String userMessage, String intent);
}
