package com.mohaemukzip.mohaemukzip_be.domain.chatbot.controller;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.ChatResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.ChatCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Chatbot", description = "챗봇 대화 및 레시피 추천 API")
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
    @Operation(summary = "챗봇 대화 요청", description = "사용자의 메시지를 분석하여 답변을 생성하고, 필요 시 맞춤형 레시피를 추천합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청 성공 (봇 응답 반환)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수값 누락 등)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    public ResponseEntity<ChatResponseDTO.ChatResultDto> processMessage(@Valid @RequestBody ChatRequestDTO.ChatMessageDto request) {
        ChatResponseDTO.ChatResultDto response = chatCommandService.processMessage(request);
        return ResponseEntity.ok(response);
    }
}
