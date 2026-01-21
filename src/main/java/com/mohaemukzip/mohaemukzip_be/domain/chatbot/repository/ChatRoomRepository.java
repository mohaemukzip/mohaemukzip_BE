package com.mohaemukzip.mohaemukzip_be.domain.chatbot.repository;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.ChatRoom;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.ChatState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByMemberIdAndState(Long memberId, ChatState state);
}
