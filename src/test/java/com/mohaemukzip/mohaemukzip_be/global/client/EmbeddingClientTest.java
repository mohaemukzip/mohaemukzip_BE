package com.mohaemukzip.mohaemukzip_be.global.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EmbeddingClientTest {

    @Autowired
    private EmbeddingClient embeddingClient;

    @Test
    void testEmbedding() {
        // 1. 파이썬 서버로 "김치볶음밥" 변환 요청
        List<Double> result = embeddingClient.getEmbedding("김치볶음밥");

        // 2. 최소 검증 (1024차원 여부 확인)
        assertNotNull(result);
        assertEquals(1024, result.size());
    }
}