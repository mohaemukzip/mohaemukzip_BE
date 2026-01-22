package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.GeminiRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.GeminiResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api-url}")
    private String apiUrl;

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String MODEL_NAME = "gemini-2.5-flash"; // 로그 확인용 상수

    public String generateChatResponse(String systemPrompt, String userPrompt) {
        GeminiRequestDTO request = GeminiRequestDTO.builder()
                .systemInstruction(GeminiRequestDTO.SystemInstruction.builder()
                        .parts(List.of(GeminiRequestDTO.Part.builder().text(systemPrompt).build()))
                        .build())
                .contents(List.of(GeminiRequestDTO.Content.builder()
                        .parts(List.of(GeminiRequestDTO.Part.builder().text(userPrompt).build()))
                        .build()))
                .build();

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();

            log.info("Gemini API 요청 시작 (Model: {}): {}", MODEL_NAME, apiUrl);
            
            String rawResponse = geminiWebClient.post()
                    .uri(uri)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Gemini API 에러 응답: {}", errorBody);
                                        // 429 에러인 경우 WebClientResponseException을 던져서 retryWhen이 잡을 수 있게 함
                                        if (clientResponse.statusCode().value() == 429) {
                                            return Mono.error(new WebClientResponseException(429, "Too Many Requests", null, null, null));
                                        }
                                        return Mono.error(new RuntimeException("Gemini API Error: " + errorBody));
                                    }))
                    .bodyToMono(String.class)
                    // 스마트 재시도 로직 적용
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)) // 최대 3회, 초기 2초 대기 (지수 백오프)
                            .filter(throwable -> throwable instanceof WebClientResponseException && 
                                    ((WebClientResponseException) throwable).getStatusCode().value() == 429) // 429 에러일 때만 재시도
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                log.error("Gemini API 재시도 횟수 초과 (Rate Limit Exceeded)");
                                return retrySignal.failure();
                            }))
                    // 우아한 실패 처리 (Fallback 전환)
                    .onErrorResume(e -> {
                        log.error("Gemini API 호출 최종 실패 - Fallback 전환: {}", e.getMessage());
                        return Mono.justOrEmpty(null); // null을 반환하여 Processor가 Fallback 처리하도록 유도
                    })
                    .doOnNext(res -> log.info("Gemini API Raw Response 수신 성공"))
                    .block(); // 동기 처리

            if (rawResponse != null) {
                log.info("Gemini Raw Response: {}", rawResponse);
                GeminiResponseDTO response = objectMapper.readValue(rawResponse, GeminiResponseDTO.class);

                if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                    GeminiResponseDTO.Candidate candidate = response.getCandidates().get(0);
                    if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                        String text = candidate.getContent().getParts().get(0).getText();
                        log.info("Gemini 응답 텍스트 추출 성공: {}", text);
                        return text;
                    }
                } else {
                    log.warn("Gemini 응답 파싱 성공했으나 candidates가 비어있음");
                }
            }
        } catch (Exception e) {
            log.error("Gemini API 호출 중 예외 발생", e);
            return null;
        }
        return null;
    }
}
