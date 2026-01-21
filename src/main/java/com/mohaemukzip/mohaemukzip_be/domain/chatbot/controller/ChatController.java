package com.mohaemukzip.mohaemukzip_be.domain.chatbot.controller;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.ChatCommandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
@Tag(name = "Chat")
@Validated
public class ChatController {

    private final ChatCommandService chatCommandService;

    /**
     * 챗봇 메시지 전송 및 응답
     * [POST] /chats
     *
     * @param request 사용자 메시지 및 회원 ID
     * @return 봇의 응답 메시지 (레시피 추천 포함)
     *
     * TODO: 추후 SecurityContextHolder를 통해 memberId를 추출하도록 리팩토링 필요
     * 현재는 Request Body로 memberId를 직접 받음
     */
    @PostMapping
    public ResponseEntity<ChatResponseDTO.ChatResultDto> processMessage(@Valid @RequestBody ChatRequestDTO.ChatMessageDto request) {
        ChatResponseDTO.ChatResultDto response = chatCommandService.processMessage(request);
        return ResponseEntity.ok(response);
    }
}
