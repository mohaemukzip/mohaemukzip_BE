package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.converter.ChatConverter;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.request.ChatPostRequest;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatProcessorResult;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.ChatResponse;
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
    public ChatResponse processMessage(Long memberId, ChatPostRequest request) {
        // 1. 채팅방 조회 또는 생성
        ChatRoom chatRoom = getOrCreateChatRoom(memberId);

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = ChatConverter.toChatMessage(chatRoom, SenderType.USER, null, request.getMessage());
        chatMessageRepository.save(userMessage);

        // 3. Processor를 통해 의도 파악 및 로직 수행 (항상 유효한 결과 반환 보장)
        String intent = chatProcessor.analyzeIntent(request.getMessage());
        ChatProcessorResult result = chatProcessor.process(chatRoom, request.getMessage(), intent);

        // 4. 봇 메시지(멘트) 저장
        ChatMessage botMessage = ChatConverter.toChatMessage(chatRoom, SenderType.BOT, result.getTitle(), result.getMessage());
        chatMessageRepository.save(botMessage);

        // 5. 최종 응답 DTO 변환
        return ChatConverter.toChatResponse(botMessage, result.getRecipes());
    }

    private ChatRoom getOrCreateChatRoom(Long memberId) {
        return chatRoomRepository.findByMemberIdAndState(memberId, ChatState.ING)
                .orElseGet(() -> {
                    ChatRoom newChatRoom = ChatConverter.toChatRoom(memberId);
                    return chatRoomRepository.save(newChatRoom);
                });
    }
}
