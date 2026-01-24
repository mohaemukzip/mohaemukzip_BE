package com.mohaemukzip.mohaemukzip_be.domain.chatbot.repository;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 메서드명 수정: findAllByChatRoomId -> findAllByChatRoom_Id (언더바 추가로 명확한 탐색)
    List<ChatMessage> findAllByChatRoom_IdOrderByCreatedAtAsc(Long chatRoomId);
}
