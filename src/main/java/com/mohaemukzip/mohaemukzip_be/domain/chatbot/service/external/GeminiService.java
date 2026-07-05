package com.mohaemukzip.mohaemukzip_be.domain.chatbot.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.request.GeminiRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response.GeminiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
public class GeminiService {

    private static final Logger chatbotMonitorLog = LoggerFactory.getLogger("CHATBOT_MONITOR");

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    @Value("${gemini.recipe.api-url}")
    private String apiUrl;

    private static final String MODEL_NAME = "gemini-2.5-flash";

    public GeminiService(
            @Qualifier("geminiRecipeWebClient") WebClient geminiWebClient,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry) {
        this.geminiWebClient = geminiWebClient;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    @CircuitBreaker(name = "gemini", fallbackMethod = "fallbackGenerateChatResponse")
    public String generateChatResponse(Long memberId, String systemPrompt, List<GeminiRequestDTO.Content> contents) {
        GeminiRequestDTO request = GeminiRequestDTO.builder()
                .systemInstruction(GeminiRequestDTO.SystemInstruction.builder()
                        .parts(List.of(GeminiRequestDTO.Part.builder().text(systemPrompt).build()))
                        .build())
                .contents(contents)
                .build();

        try {
            log.info("Gemini API 요청 시작 (Model: {}): {}", MODEL_NAME, apiUrl);
            
            String rawResponse = geminiWebClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Gemini API 에러 응답: {}", errorBody);
                                        if (clientResponse.statusCode().value() == 429) {
                                            return Mono.error(new WebClientResponseException(429, "Too Many Requests", null, null, null));
                                        }
                                        return Mono.error(new RuntimeException("Gemini API Error: " + errorBody));
                                    }))
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException && 
                                    ((WebClientResponseException) throwable).getStatusCode().value() == 429)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                log.error("Gemini API 재시도 횟수 초과 (Rate Limit Exceeded)");
                                return retrySignal.failure();
                            }))
                    .onErrorResume(e -> {
                        log.error("Gemini API 호출 최종 실패 - Fallback 전환: {}", e.getMessage());
                        return Mono.error(e);
                    })
                    .doOnNext(res -> log.info("Gemini API Raw Response 수신 성공"))
                    .block(Duration.ofSeconds(30));

            if (rawResponse != null) {
                log.debug("Gemini Raw Response length: {}", rawResponse.length());
                
                GeminiResponseDTO response = objectMapper.readValue(rawResponse, GeminiResponseDTO.class);

                if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                    GeminiResponseDTO.Candidate candidate = response.getCandidates().get(0);
                    if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                        String text = candidate.getContent().getParts().get(0).getText();
                        // 보안 강화: 전체 내용은 DEBUG 레벨로, INFO 레벨에는 길이만 출력
                        log.debug("Gemini 응답 텍스트: {}", text);
                        log.info("Gemini 응답 텍스트 추출 성공 (length: {})", text.length());
                        
                        // [모니터링] 토큰 사용량 비동기 JSON 로깅
                        if (response.getUsageMetadata() != null) {
                            int totalTokens = response.getUsageMetadata().getTotalTokenCount();
                            int promptTokens = response.getUsageMetadata().getPromptTokenCount();
                            int completionTokens = response.getUsageMetadata().getCandidatesTokenCount();
                            chatbotMonitorLog.info("{\"action\": \"CHATBOT_USAGE\", \"memberId\": {}, \"promptTokens\": {}, \"completionTokens\": {}, \"totalTokens\": {}, \"status\": \"SUCCESS\"}",
                                    memberId, promptTokens, completionTokens, totalTokens);
                                    
                            meterRegistry.counter("gemini_api_tokens_total", "model", MODEL_NAME).increment(totalTokens);
                        }
                        
                        return text;
                    }
                } else {
                    log.warn("Gemini 응답 파싱 성공했으나 candidates가 비어있음");
                }
            }
        } catch (Exception e) {
            log.error("Gemini API 호출 중 예외 발생", e);
            throw new RuntimeException("Gemini API Call Failed", e);
        }
        return null;
    }

    public String fallbackGenerateChatResponse(Long memberId, String systemPrompt, List<GeminiRequestDTO.Content> contents, Throwable t) {
        log.error("Gemini API 서킷 브레이커 발동! Fallback 실행 - 원인: {}", t.getMessage());
        throw new com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException(
                com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus.SERVICE_UNAVAILABLE
        );
    }
}
