package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// redis에 저장할 메시지 객체
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedisChatMessage {
    private SenderType sender;
    private String title;   // 봇 응답 제목 (사용자는 null)
    private String content; // 메시지 내용
    private String timestamp;
}
