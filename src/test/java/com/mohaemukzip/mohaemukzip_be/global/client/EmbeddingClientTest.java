package com.mohaemukzip.mohaemukzip_be.global.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class EmbeddingClientTest {

    @Autowired
    private EmbeddingClient embeddingClient;

    @Test
    void testEmbedding() {
        // 1. 파이썬 서버로 "김치볶음밥" 변환 요청
        List<Double> result = embeddingClient.getEmbedding("김치볶음밥");

        // 2. 결과 출력
        System.out.println("=====================================");
        System.out.println("임베딩 크기: " + result.size());
        System.out.println("임베딩 앞 5개 숫자: " + result.subList(0, 5));
        System.out.println("=====================================");
    }
}