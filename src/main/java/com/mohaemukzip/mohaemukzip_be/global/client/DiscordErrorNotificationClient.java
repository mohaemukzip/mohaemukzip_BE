package com.mohaemukzip.mohaemukzip_be.global.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * 서버 에러(5xx) 전용 Discord 웹훅 클라이언트.
 */
@Slf4j
@Component
public class DiscordErrorNotificationClient {

    private final WebClient webClient;
    private final String webhookUrl;

    public DiscordErrorNotificationClient(WebClient.Builder webClientBuilder,
                                          @Value("${discord.error-webhook.url:}") String webhookUrl) {
        this.webClient = webClientBuilder.build();
        this.webhookUrl = webhookUrl;
    }

    public void sendEmbed(Map<String, Object> embed) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord 에러 웹훅 URL이 설정되어 있지 않습니다. 에러 알림을 무시합니다.");
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "embeds", List.of(embed),
                    "allowed_mentions", Map.of("parse", List.of())
            );

            webClient.post()
                    .uri(webhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(v -> log.info("Discord 에러 알림 전송 성공"))
                    .doOnError(e -> log.error("Discord 에러 알림 전송 실패: {}", e.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Discord 에러 알림 발송 중 예외 발생: {}", e.getMessage());
        }
    }
}
