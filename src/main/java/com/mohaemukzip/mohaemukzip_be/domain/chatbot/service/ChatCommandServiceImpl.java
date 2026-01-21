package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.converter.ChatConverter;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatMessage;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatRoom;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.ChatState;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.repository.ChatMessageRepository;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatCommandServiceImpl implements ChatCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatProcessor chatProcessor;

    @Override
    @Transactional
    public ChatResponseDTO.ChatResultDto processMessage(ChatRequestDTO.ChatMessageDto request) {
        // 1. 채팅방 조회 또는 생성
        ChatRoom chatRoom = chatRoomRepository.findByMemberIdAndState(request.getMemberId(), ChatState.ING)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatConverter.toChatRoom(request.getMemberId());
                    return chatRoomRepository.save(newChatRoom);
                });

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = ChatConverter.toChatMessage(chatRoom, SenderType.USER, request.getMessage());
        chatMessageRepository.save(userMessage);

        // 3. Processor를 통해 의도 파악 및 로직 수행
        String intent = chatProcessor.analyzeIntent(request.getMessage());
        // 수정됨: request.getMessage()를 userMessage 인자로 전달
        ChatProcessorResult result = chatProcessor.process(chatRoom, request.getMessage(), intent);

        // 4. 봇 메시지(멘트) 저장
        ChatMessage botMessage = ChatConverter.toChatMessage(chatRoom, SenderType.BOT, result.getMessage());
        chatMessageRepository.save(botMessage);

        // 5. 최종 응답 DTO 변환 (레시피 리스트 포함)
        return ChatConverter.toChatResultDto(botMessage, result.getRecipes());
    }
}
