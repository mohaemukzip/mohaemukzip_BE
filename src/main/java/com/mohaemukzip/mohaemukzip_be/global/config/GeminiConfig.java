package com.mohaemukzip.mohaemukzip_be.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

@Configuration
public class GeminiConfig {

    @Value("${gemini.recipe.api-url}")
    private String recipeApiUrl;

    @Value("${gemini.recipe.api-key}")
    private String recipeApiKey;

    @Value("${gemini.summary.api-url}")
    private String summaryApiUrl;

    @Value("${gemini.summary.api-key}")
    private String summaryApiKey;

    @Value("${gemini.recipe.timeout.connect:10}")
    private int connectTimeout;

    @Value("${gemini.recipe.timeout.response:30}")
    private int responseTimeout;

    @Bean(name = "geminiRecipeWebClient")
    public WebClient geminiRecipeWebClient() {
        return createWebClient(recipeApiUrl, recipeApiKey);
    }

    @Bean(name = "geminiSummaryWebClient")
    public WebClient geminiSummaryWebClient() {
        return createWebClient(summaryApiUrl, summaryApiKey);
    }

    private WebClient createWebClient(String baseUrl, String apiKey) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout * 1000)
                .responseTimeout(Duration.ofSeconds(responseTimeout));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }
}