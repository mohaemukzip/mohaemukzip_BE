package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.OpenAiRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.OpenAiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final WebClient openAiWebClient;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api-url}")
    private String apiUrl;

    public String generateChatResponse(String systemPrompt, String userPrompt) {
        OpenAiRequestDTO request = OpenAiRequestDTO.builder()
                .model(model)
                .messages(List.of(
                        OpenAiRequestDTO.Message.builder().role("system").content(systemPrompt).build(),
                        OpenAiRequestDTO.Message.builder().role("user").content(userPrompt).build()
                ))
                .max_tokens(500)
                .temperature(0.7)
                .build();

        try {
            OpenAiResponseDTO response = openAiWebClient.post()
                    .uri(apiUrl)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAiResponseDTO.class)
                    .block(); // 동기 처리 (Processor 흐름상 필요)

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage());
            return null; // Fallback 처리를 위해 null 반환
        }
        return null;
    }
}
