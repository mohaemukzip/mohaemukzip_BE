package com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false)
    private SenderType senderType;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
}
