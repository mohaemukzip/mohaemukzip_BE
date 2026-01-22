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

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatCommandServiceImpl implements ChatCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatProcessor chatProcessor;

    @Override
    @Transactional
    public ChatResponseDTO.ChatResultDto processMessage(Long memberId, ChatRequestDTO.ChatMessageDto request) {
        // 1. 채팅방 조회 또는 생성 (인증된 memberId 사용)
        ChatRoom chatRoom = chatRoomRepository.findByMemberIdAndState(memberId, ChatState.ING)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatConverter.toChatRoom(memberId);
                    return chatRoomRepository.save(newChatRoom);
                });

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = ChatConverter.toChatMessage(chatRoom, SenderType.USER, request.getMessage());
        chatMessageRepository.save(userMessage);

        // 3. Processor를 통해 의도 파악 및 로직 수행
        String intent = chatProcessor.analyzeIntent(request.getMessage());
        ChatProcessorResult result = chatProcessor.process(chatRoom, request.getMessage(), intent);

        // 방어적 코드 추가: Processor 결과가 null일 경우 기본 응답 처리
        if (result == null) {
            result = ChatProcessorResult.builder()
                    .message("죄송해요, 처리 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
                    .recipes(Collections.emptyList())
                    .build();
        }

        // 4. 봇 메시지(멘트) 저장
        ChatMessage botMessage = ChatConverter.toChatMessage(chatRoom, SenderType.BOT, result.getMessage());
        chatMessageRepository.save(botMessage);

        // 5. 최종 응답 DTO 변환 (레시피 리스트 포함)
        return ChatConverter.toChatResultDto(botMessage, result.getRecipes());
    }
}
