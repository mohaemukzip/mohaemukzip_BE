package com.mohaemukzip.mohaemukzip_be.global.controller;

import com.mohaemukzip.mohaemukzip_be.global.client.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmbeddingTestController {

    private final EmbeddingClient embeddingClient;

    // 브라우저에서 /test-embedding 주소로 접속하면 이 메서드가 실행됩니다.
    @GetMapping("/test-embedding")
    public List<Double> testEmbedding(@RequestParam(defaultValue = "치즈 떡볶이") String text) {
        // 클라이언트를 통해 파이썬 서버로 텍스트를 보내고 결과를 받아옵니다.
        return embeddingClient.getEmbedding(text);
    }
}