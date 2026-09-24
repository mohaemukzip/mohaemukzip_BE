package com.mohaemukzip.mohaemukzip_be.global.client.transcript;

import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;

/**
 * 영상 자체에 자막이 없는 정상적인 비즈니스 케이스를 나타낸다.
 * RapidAPI 장애(5xx, 429, 네트워크 오류)와 구분하기 위해 별도 타입으로 분리했으며,
 * resilience4j의 rapidapi 서킷 브레이커 실패 집계에서 제외(ignoreExceptions)된다.
 */
public class TranscriptNotFoundException extends BusinessException {
    public TranscriptNotFoundException() {
        super(ErrorStatus.TRANSCRIPT_NOT_AVAILABLE);
    }
}
