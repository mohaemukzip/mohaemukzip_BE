package com.mohaemukzip.mohaemukzip_be.global.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Slf4j
@Component
public class DiscordNotificationClient {

    private final WebClient webClient;
    private final String webhookUrl;

    public DiscordNotificationClient(WebClient.Builder webClientBuilder, 
                                     @Value("${discord.webhook.url:}") String webhookUrl) {
        this.webClient = webClientBuilder.build();
        this.webhookUrl = webhookUrl;
    }

    public void sendNotification(String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord 웹훅 URL이 설정되어 있지 않습니다. 알림을 무시합니다.");
            return;
        }

        try {
            Map<String, String> body = Map.of("content", message);

            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(v -> log.info("Discord 웹훅 알림 전송 성공"))
                    .doOnError(e -> log.error("Discord 웹훅 알림 전송 실패: {}", e.getMessage()))
                    .subscribe(); // Non-blocking fire and forget
        } catch (Exception e) {
            log.error("Discord 웹훅 알림 발송 중 예외 발생: {}", e.getMessage());
        }
    }
}
