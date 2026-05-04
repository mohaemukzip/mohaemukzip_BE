package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.helper;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.RedisChatMessage;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.request.GeminiRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 챗봇 대화 컨텍스트 구성을 도와주는 헬퍼 클래스.
 */
@Component
public class ChatContextHelper {

    /**
     * 이전 대화 기록을 Gemini 요청 포맷으로 변환하며, 최근 N턴으로 제한합니다.
     *
     * @param history  전체 대화 기록
     * @param maxTurns 유지할 최대 대화 턴 수
     * @return Gemini 요청에 포함될 Content 리스트
     */
    public List<GeminiRequestDTO.Content> buildHistoryContents(List<RedisChatMessage> history, int maxTurns) {
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }

        List<GeminiRequestDTO.Content> contents = new ArrayList<>();
        
        // 최근 N턴 계산
        int fromIndex = Math.max(0, history.size() - maxTurns);
        List<RedisChatMessage> recentHistory = history.subList(fromIndex, history.size());

        for (RedisChatMessage msg : recentHistory) {
            String role = msg.getSender() == SenderType.USER ? "user" : "model";
            String text = msg.getSender() == SenderType.USER
                    ? msg.getContent()
                    : (msg.getTitle() != null ? msg.getTitle() + " ||| " + msg.getContent() : msg.getContent());

            contents.add(GeminiRequestDTO.Content.builder()
                    .role(role)
                    .parts(List.of(GeminiRequestDTO.Part.builder().text(text).build()))
                    .build());
        }

        return contents;
    }
}
