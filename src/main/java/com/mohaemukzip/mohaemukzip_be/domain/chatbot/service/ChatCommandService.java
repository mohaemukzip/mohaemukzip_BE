package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatResponseDTO;

public interface ChatCommandService {
    // memberId 파라미터 추가 (Controller에서 인증된 ID를 넘겨받음)
    ChatResponseDTO.ChatResultDto processMessage(Long memberId, ChatRequestDTO.ChatMessageDto request);
}
