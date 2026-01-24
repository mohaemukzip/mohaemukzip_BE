package com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.ChatState;
import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private ChatState state;

    public void updateState(ChatState state) {
        this.state = state;
    }
}
