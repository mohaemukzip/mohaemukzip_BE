package com.mohaemukzip.mohaemukzip_be.global.exception;

import com.mohaemukzip.mohaemukzip_be.global.client.DiscordErrorNotificationClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 5xx 응답이 발생했을 때 Discord로 알림을 보낸다.
 * 같은 위치에서 발생한 동일 예외는 일정 시간 동안 한 번만 알려 알림 폭탄을 막는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ServerErrorNotifier {

    private static final int COLOR_ERROR = 0xE74C3C;       // 500: 빨강
    private static final int COLOR_UNAVAILABLE = 0xE67E22; // 503: 주황
    private static final long DEDUP_WINDOW_MILLIS = 60_000L;
    private static final int DEDUP_MAX_SIZE = 500;
    private static final String APP_PACKAGE = "com.mohaemukzip";

    private final DiscordErrorNotificationClient discordClient;

    private final Map<String, Long> recentlySent = new ConcurrentHashMap<>();

    public void notify(HttpStatusCode status, Throwable ex) {
        if (!status.is5xxServerError()) {
            return;
        }

        try {
            HttpServletRequest request = currentRequest();
            String method = request != null ? request.getMethod() : "-";
            String uri = request != null ? request.getRequestURI() : "-";
            String origin = findOrigin(ex);

            if (isDuplicate(ex.getClass().getName() + "|" + method + "|" + uri + "|" + origin)) {
                return;
            }

            discordClient.sendEmbed(buildEmbed(status, ex, method, uri, origin));
        } catch (Exception e) {
            // 알림 실패가 원래 에러 응답에 영향을 주면 안 된다.
            log.error("5xx Discord 알림 생성 중 예외 발생: {}", e.getMessage());
        }
    }

    private Map<String, Object> buildEmbed(HttpStatusCode status, Throwable ex,
                                           String method, String uri, String origin) {
        int code = status.value();

        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(field("📍 요청", "`" + method + " " + escapeCode(uri) + "`", false));
        fields.add(field("📄 위치", "`" + escapeCode(origin) + "`", false));
        fields.add(field("💬 요약", summarize(ex), false));
        fields.add(field("🔎 원인", "`" + truncate(escapeCode(oneLine(rootCause(ex))), 150) + "`", false));

        return Map.of(
                "title", (code == 503 ? "⚠️ " : "🚨 ") + code + " 서버 에러",
                "color", code == 503 ? COLOR_UNAVAILABLE : COLOR_ERROR,
                "fields", fields,
                "timestamp", Instant.now().toString()
        );
    }

    /** 예외를 한글 한 줄로 요약한다. */
    private String summarize(Throwable ex) {
        Throwable root = rootCause(ex);
        String all = (ex.getClass().getName() + " " + ex.getMessage() + " "
                + root.getClass().getName() + " " + root.getMessage());

        if (ex instanceof BusinessException be) {
            return be.getBaseCode().getMessage();
        }
        if (ex instanceof GlobalException ge) {
            return ge.getBaseCode().getMessage();
        }
        if (ex instanceof CallNotPermittedException) {
            return "외부 API 서킷브레이커가 열려 호출이 차단됐어요.";
        }
        if (all.contains("Unable to acquire JDBC Connection") || all.contains("Communications link failure")
                || all.contains("Access denied for user") || all.contains("CannotGetJdbcConnection")) {
            return "DB 연결에 실패했어요.";
        }
        if (all.contains("doesn't exist") || all.contains("Unknown column")) {
            return "DB에 없는 테이블/컬럼을 사용했어요.";
        }
        if (all.contains("Timeout") || all.contains("timed out")) {
            return "응답 시간이 초과됐어요.";
        }
        if (all.contains("WebClientResponseException") || all.contains("RestClientException")) {
            return "외부 API 호출에 실패했어요.";
        }
        if (all.contains("org.springframework.dao") || all.contains("org.hibernate")) {
            return "DB 쿼리 실행 중 오류가 발생했어요.";
        }
        if (ex instanceof NullPointerException) {
            return "null 값을 참조했어요.";
        }
        if (ex instanceof ClassCastException) {
            return "잘못된 타입 변환이 발생했어요.";
        }
        return "예상하지 못한 서버 오류가 발생했어요.";
    }

    private Throwable rootCause(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t;
    }

    private String oneLine(Throwable t) {
        String msg = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
        return msg.replaceAll("\s+", " ").trim();
    }

    private Map<String, Object> field(String name, String value, boolean inline) {
        return Map.of("name", name, "value", value, "inline", inline);
    }

    /** 스택에서 우리 앱 코드 중 가장 먼저 나오는 프레임 (없으면 예외가 던져진 첫 프레임). */
    private String findOrigin(Throwable ex) {
        StackTraceElement[] stack = ex.getStackTrace();
        return Arrays.stream(stack)
                .filter(e -> e.getClassName().startsWith(APP_PACKAGE))
                .findFirst()
                .or(() -> Arrays.stream(stack).findFirst())
                .map(e -> e.getClassName().substring(e.getClassName().lastIndexOf('.') + 1)
                        + "." + e.getMethodName() + ":" + e.getLineNumber())
                .orElse("-");
    }

    private boolean isDuplicate(String key) {
        long now = System.currentTimeMillis();
        if (recentlySent.size() > DEDUP_MAX_SIZE) {
            recentlySent.values().removeIf(t -> now - t > DEDUP_WINDOW_MILLIS);
        }
        Long last = recentlySent.get(key);
        if (last != null && now - last < DEDUP_WINDOW_MILLIS) {
            return true;
        }
        recentlySent.put(key, now);
        return false;
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        return attrs instanceof ServletRequestAttributes sra ? sra.getRequest() : null;
    }

    private String escapeCode(String text) {
        return text.replace("`", "'");
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }
}
