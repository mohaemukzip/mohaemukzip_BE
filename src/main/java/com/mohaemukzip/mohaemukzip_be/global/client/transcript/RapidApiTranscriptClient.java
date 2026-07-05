package com.mohaemukzip.mohaemukzip_be.global.client.transcript;

import com.mohaemukzip.mohaemukzip_be.global.client.transcript.dto.RapidApiTranscriptResponse;
import com.mohaemukzip.mohaemukzip_be.global.client.transcript.dto.TranscriptSegment;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import io.micrometer.core.instrument.MeterRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.concurrent.atomic.AtomicInteger;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class RapidApiTranscriptClient implements TranscriptClient {

    private final WebClient webClient;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger rapidApiRemaining = new AtomicInteger(0);

    public RapidApiTranscriptClient(@Qualifier("rapidApiWebClient") WebClient webClient, MeterRegistry meterRegistry) {
        this.webClient = webClient;
        this.meterRegistry = meterRegistry;
        this.meterRegistry.gauge("rapidapi_requests_remaining", rapidApiRemaining);
    }

    @Override
    @CircuitBreaker(name = "rapidapi", fallbackMethod = "fallbackFetchTranscript")
    public List<TranscriptSegment> fetchTranscript(String videoId) {
        log.info("RapidAPI 자막 조회 시작 - videoId: {}", videoId);

        ResponseEntity<RapidApiTranscriptResponse> responseEntity = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/transcript")
                        .queryParam("videoId", videoId)
                        .queryParam("lang", "ko")
                        .build())
                .retrieve()
                .onStatus(this::isAuthOrEndpointError, this::handleAuthOrEndpointError)
                .onStatus(HttpStatusCode::isError, this::handleGenericHttpError)
                .toEntity(RapidApiTranscriptResponse.class)
                .onErrorMap(e -> !(e instanceof BusinessException),
                        e -> {
                            meterRegistry.counter("rapidapi_requests_total", "status", "error").increment();
                            log.error("자막 추출 중 예기치 못한 오류 - videoId: {}", videoId, e);
                            return new BusinessException(ErrorStatus.INTERNAL_SERVER_ERROR);
                        })
                .block(Duration.ofSeconds(12));

        meterRegistry.counter("rapidapi_requests_total", "status", "success").increment();

        if (responseEntity != null && responseEntity.getHeaders().containsKey("X-RateLimit-Requests-Remaining")) {
            try {
                String remainingStr = responseEntity.getHeaders().getFirst("X-RateLimit-Requests-Remaining");
                if (remainingStr != null) {
                    rapidApiRemaining.set(Integer.parseInt(remainingStr));
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse X-RateLimit-Requests-Remaining header", e);
            }
        }

        return validateTranscript(responseEntity != null ? responseEntity.getBody() : null, videoId);
    }

    public List<TranscriptSegment> fallbackFetchTranscript(String videoId, Throwable t) {
        log.error("RapidAPI 서킷 브레이커 발동! Fallback 실행 - videoId: {}, 원인: {}", videoId, t.getMessage());
        throw new RuntimeException("현재 AI 자막 추출 서비스가 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
    }

    // HTTP 상태코드 분류 (책임 2 보조)
    private boolean isAuthOrEndpointError(HttpStatusCode status) {
        return status == HttpStatus.NOT_FOUND
                || status == HttpStatus.FORBIDDEN
                || status == HttpStatus.UNAUTHORIZED;
    }

    private Mono<? extends Throwable> handleAuthOrEndpointError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("(empty body)")
                .doOnNext(body -> log.error("RapidAPI 인증/엔드포인트 오류 - status: {}, body: {}", clientResponse.statusCode(), body))
                .thenReturn(new BusinessException(ErrorStatus.EXTERNAL_API_ERROR));
    }

    private Mono<? extends Throwable> handleGenericHttpError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .defaultIfEmpty("(empty body)")
                .doOnNext(body -> log.error("RapidAPI 통신 오류 - status: {}, body: {}", clientResponse.statusCode(), body))
                .thenReturn(new BusinessException(ErrorStatus.TRANSCRIPT_NOT_AVAILABLE));
    }

    // 응답 검증 (책임 3)
    private List<TranscriptSegment> validateTranscript(RapidApiTranscriptResponse response, String videoId) {
        if (response == null || !response.success()
                || response.transcript() == null || response.transcript().isEmpty()) {
            log.warn("자막 데이터 없음 또는 API 응답 실패 - videoId: {}", videoId);
            throw new BusinessException(ErrorStatus.TRANSCRIPT_NOT_AVAILABLE);
        }
        return response.transcript();
    }
}
