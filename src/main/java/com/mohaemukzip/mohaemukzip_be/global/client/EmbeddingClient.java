package com.mohaemukzip.mohaemukzip_be.global.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Gemini API와 통신하여 텍스트를 임베딩(벡터)으로 변환하는 클라이언트.
 */
@Slf4j
@Component
public class EmbeddingClient {

    private final WebClient webClient;
    private final String geminiApiUrl;

    public EmbeddingClient(
            @Value("${gemini.embedding.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-2:embedContent}") String geminiApiUrl,
            @Value("${gemini.recipe.api-key}") String geminiApiKey,
            WebClient.Builder webClientBuilder) {

        this.geminiApiUrl = geminiApiUrl;
        this.webClient = webClientBuilder
                .defaultHeader("x-goog-api-key", geminiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("[EmbeddingClient] Gemini 임베딩 클라이언트 초기화 완료");
    }

    /**
     * Gemini API로 텍스트를 보내고 임베딩 벡터를 받아옵니다.
     *
     * @param text 임베딩할 텍스트
     * @return 768차원의 임베딩 벡터
     */
    public List<Double> getEmbedding(String text) {
        try {
            // Gemini API 요청 Body 구성 (gemini-embedding-2 모델 및 768차원 지정)
            Map<String, Object> requestBody = Map.of(
                    "model", "models/gemini-embedding-2",
                    "outputDimensionality", 768,
                    "content", Map.of(
                            "parts", List.of(Map.of("text", text))
                    )
            );

            // API 호출 (최대 10초 대기)
            GeminiEmbeddingResponse response = webClient.post()
                    .uri(geminiApiUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiEmbeddingResponse.class)
                    .block(Duration.ofSeconds(10));

            if (response == null || response.embedding() == null || response.embedding().values() == null) {
                throw new RuntimeException("Gemini 서버로부터 임베딩을 받지 못했습니다.");
            }

            return response.embedding().values();

        } catch (Exception e) {
            log.error("[EmbeddingClient] Gemini 임베딩 추출 중 오류 발생 - text: {}", text, e);
            throw new RuntimeException("Gemini Embedding API call failed", e);
        }
    }

    // Gemini의 JSON 응답을 매핑할 내부 Record
    record GeminiEmbeddingResponse(EmbeddingData embedding) {}
    record EmbeddingData(List<Double> values) {}
}