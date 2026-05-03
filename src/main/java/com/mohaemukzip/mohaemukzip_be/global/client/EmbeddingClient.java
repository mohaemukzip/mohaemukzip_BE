package com.mohaemukzip.mohaemukzip_be.global.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingClient {

    // (팁) 나중에 application.yml에 파이썬 서버 URL을 빼두시면 배포 시 관리가 편합니다.
    private final String PYTHON_SERVER_URL = "http://localhost:8000";

    /**
     * FastAPI 서버로 텍스트를 보내고 임베딩 벡터를 받아옵니다.
     * @param text 임베딩할 텍스트 (예: "치즈 떡볶이")
     * @return 1024차원의 임베딩 벡터
     */
    public List<Double> getEmbedding(String text) {
        try {
            // WebClient를 이용해 동기(block) 방식으로 요청합니다.
            WebClient webClient = WebClient.create(PYTHON_SERVER_URL);

            // FastAPI에서 정의한 {"text": "..."} 형태의 Body를 만듭니다.
            Map<String, String> requestBody = Map.of("text", text);

            // API 호출
            EmbeddingResponse response = webClient.post()
                    .uri("/embed")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .block(); // 요청이 끝날 때까지 기다림

            if (response == null || response.embedding() == null) {
                throw new RuntimeException("파이썬 서버로부터 임베딩을 받지 못했습니다.");
            }

            return response.embedding();

        } catch (Exception e) {
            log.error("임베딩 추출 중 오류 발생. text: {}", text, e);
            throw new RuntimeException("Embedding API call failed", e);
        }
    }

    // 파이썬 서버의 JSON 응답({"embedding": [...]})을 매핑할 내부 Record
    record EmbeddingResponse(List<Double> embedding) {}
}