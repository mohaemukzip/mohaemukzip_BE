package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ChatRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageDto {
        @NotNull(message = "Member ID는 필수입니다.")
        private Long memberId;

        @NotBlank(message = "메시지를 입력해주세요.")
        private String message;
    }
}
