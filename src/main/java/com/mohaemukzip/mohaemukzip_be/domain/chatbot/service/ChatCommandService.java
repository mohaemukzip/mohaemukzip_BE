package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatResponseDTO;

public interface ChatCommandService {
    ChatResponseDTO.ChatResultDto processMessage(ChatRequestDTO.ChatMessageDto request);
}
