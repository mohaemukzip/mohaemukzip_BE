package com.mohaemukzip.mohaemukzip_be.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeTranscriptService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 가장 단순한 timedtext VTT 시도:
     * - ko 자막 우선, 없으면 en 시도
     * - 실패하면 예외
     */
    public String fetchTranscriptVtt(String videoId) {
        String vtt = tryFetch(videoId, "ko");
        if (vtt != null) return vtt;

        vtt = tryFetch(videoId, "en");
        if (vtt != null) return vtt;

        throw new IllegalStateException("자막을 가져올 수 없습니다. (자막 없음/접근 제한/YouTube 변경) videoId=" + videoId);
    }

    private String tryFetch(String videoId, String lang) {
        try {
            // fmt=vtt 는 vtt 캡션 포맷 요청(비공식 timedtext)
            String url = "https://www.youtube.com/api/timedtext?v=" + encode(videoId)
                    + "&lang=" + encode(lang)
                    + "&fmt=vtt";

            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);

            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
                return null;
            }

            String body = res.getBody().trim();
            // 자막 없으면 빈 응답이 올 수 있음
            if (body.isBlank() || !body.startsWith("WEBVTT")) {
                return null;
            }
            return body;
        } catch (Exception e) {
            log.warn("timedtext 자막 조회 실패 videoId={}, lang={}, err={}", videoId, lang, e.getMessage());
            return null;
        }
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
