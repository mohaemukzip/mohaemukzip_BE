package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.command;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.request.ChatPostRequest;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatResponse;

public interface ChatCommandService {
    ChatResponse processMessage(Long memberId, ChatPostRequest request);
}
