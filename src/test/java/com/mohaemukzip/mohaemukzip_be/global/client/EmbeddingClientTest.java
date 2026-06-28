package com.mohaemukzip.mohaemukzip_be.global.client;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("외부 API 통신으로 인해 CI 환경에서 실패하므로 비활성화")
@SpringBootTest
class EmbeddingClientTest {

    @Autowired
    private EmbeddingClient embeddingClient;

    @Test
    void testEmbedding() {
        // 1. 파이썬 서버로 "김치볶음밥" 변환 요청 (현재는 Gemini API 768차원 모델 사용)
        List<Double> result = embeddingClient.getEmbedding("김치볶음밥");

        // 2. 최소 검증 (768차원 여부 확인)
        assertNotNull(result);
        assertEquals(768, result.size());
    }
}