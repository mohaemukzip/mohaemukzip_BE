package com.mohaemukzip.mohaemukzip_be.global.client.transcript;

import com.mohaemukzip.mohaemukzip_be.global.client.transcript.dto.RapidApiTranscriptResponse;
import com.mohaemukzip.mohaemukzip_be.global.client.transcript.dto.TranscriptSegment;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Component
public class RapidApiTranscriptClient implements TranscriptClient {

    private final WebClient webClient;

    public RapidApiTranscriptClient(@Qualifier("rapidApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public List<TranscriptSegment> fetchTranscript(String videoId) {
        log.info("RapidAPI 자막 조회 시작 - videoId: {}", videoId);

        try {
            RapidApiTranscriptResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/transcript")
                            .queryParam("videoId", videoId)
                            .queryParam("lang", "ko") // 한국어 요청 복구
                            .build())
                    .retrieve()
                    .bodyToMono(RapidApiTranscriptResponse.class)
                    .block();

            if (response == null || !response.success() || response.transcript() == null || response.transcript().isEmpty()) {
                log.warn("자막 데이터 없음 또는 API 응답 실패 - videoId: {}", videoId);
                throw new BusinessException(ErrorStatus.TRANSCRIPT_NOT_AVAILABLE);
            }

            return response.transcript();

        } catch (WebClientResponseException e) {
            log.error("RapidAPI 통신 오류 - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            // 에러 세분화 (404/403 등)
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException(ErrorStatus.EXTERNAL_API_ERROR); // API 엔드포인트 틀렸을 때 명확히 구분
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN || e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new BusinessException(ErrorStatus.EXTERNAL_API_ERROR); // 인증/할당량 오류
            }
            
            throw new BusinessException(ErrorStatus.TRANSCRIPT_NOT_AVAILABLE); // 기타(400 등)는 자막 추출 실패로 간주
        } catch (Exception e) {
            log.error("자막 추출 중 예외 발생 - videoId: {}, 에러: {}", videoId, e.getMessage(), e);
            throw new BusinessException(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
