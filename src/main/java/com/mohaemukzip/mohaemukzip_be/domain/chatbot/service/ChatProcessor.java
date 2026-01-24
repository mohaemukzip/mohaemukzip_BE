package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatRoom;

public interface ChatProcessor {
    String analyzeIntent(String userMessage);
    ChatProcessorResult process(ChatRoom chatRoom, String userMessage, String intent);
}
