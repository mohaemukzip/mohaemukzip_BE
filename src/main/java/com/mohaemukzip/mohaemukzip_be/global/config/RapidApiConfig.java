package com.mohaemukzip.mohaemukzip_be.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RapidApiConfig {

    // 시스템 환경변수 RAPIDAPI_KEY를 주입받습니다. 설정되지 않았다면 빈 문자열("")을 가집니다.
    @Value("${RAPIDAPI_KEY:}")
    private String apiKey;

    @Bean(name = "rapidApiWebClient")
    public WebClient rapidApiWebClient(WebClient.Builder builder) {
        // 호스트와 URL은 변하지 않는 값이므로, application.yaml 로드 실패와 무관하게 동작하도록 하드코딩합니다.
        return builder
                .baseUrl("https://youtube-transcript3.p.rapidapi.com")
                .defaultHeader("X-RapidAPI-Key", apiKey)
                .defaultHeader("X-RapidAPI-Host", "youtube-transcript3.p.rapidapi.com")
                .build();
    }
}
