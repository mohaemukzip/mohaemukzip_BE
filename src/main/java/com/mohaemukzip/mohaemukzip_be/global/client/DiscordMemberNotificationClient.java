package com.mohaemukzip.mohaemukzip_be.global.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DiscordMemberNotificationClient {

    private final WebClient webClient;
    private final String webhookUrl;

    public DiscordMemberNotificationClient(
            WebClient.Builder webClientBuilder,
            @Value("${discord.member-webhook.url:}") String webhookUrl
    ) {
        this.webClient = webClientBuilder.build();
        this.webhookUrl = webhookUrl;
    }

    public void sendMemberNotification(
            String title,
            int color,
            String nickname,
            String loginType,
            Long memberId,
            long totalMembers,
            String time
    ) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord 회원 알림 웹훅 URL이 설정되어 있지 않습니다.");
            return;
        }

        Map<String, Object> embed = Map.of(
                "title", title,
                "color", color,
                "fields", List.of(
                        Map.of(
                                "name", "👤 닉네임",
                                "value", escapeText(nickname),
                                "inline", false
                        ),
                        Map.of(
                                "name", "가입 방식",
                                "value", escapeText(loginType),
                                "inline", false
                        ),
                        Map.of(
                                "name", "👥 현재 가입자 수",
                                "value", "**" + totalMembers + "명**",
                                "inline", false
                        ),
                        Map.of(
                                "name", " ",
                                "value", time,
                                "inline", false
                        )
                )
        );

        Map<String, Object> payload = Map.of(
                "embeds", List.of(embed),
                "allowed_mentions", Map.of("parse", List.of())
        );

        webClient.post()
                .uri(webhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response ->
                        log.info("Discord 회원 Embed 알림 전송 성공: {}", title))
                .doOnError(error ->
                        log.error("Discord 회원 Embed 알림 전송 실패: {}",
                                error.getMessage()))
                .subscribe();
    }

    private String escapeText(String text) {
        if (text == null || text.isBlank()) {
            return "(없음)";
        }

        return text.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("_", "\\_")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace("|", "\\|")
                .replace(">", "\\>");
    }
}
