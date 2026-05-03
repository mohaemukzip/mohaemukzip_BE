package com.mohaemukzip.mohaemukzip_be.global.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JPA AttributeConverter: List<Double> ↔ JSON 문자열 (TEXT 컬럼)
 *
 * MySQL은 List<Double> 같은 Java 컬렉션 타입을 직접 저장할 수 없습니다.
 * 이 Converter가 엔티티를 저장할 때는  → JSON 문자열("[0.1, 0.2, ...]")로 변환하고,
 * 불러올 때는 JSON 문자열 → List<Double>로 역변환 해줍니다.
 *
 * autoApply = false: @Convert(converter = DoubleListConverter.class)를 붙인 필드에만 적용됩니다.
 */
@Slf4j
@Converter
public class DoubleListConverter implements AttributeConverter<List<Double>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 엔티티 → DB 저장 시: List<Double> → JSON 문자열
     * 예) [0.123, 0.456, ...] → "[0.123,0.456,...]"
     */
    @Override
    public String convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("임베딩 벡터를 JSON 문자열로 변환하는 중 오류 발생", e);
            throw new IllegalArgumentException("List<Double> → JSON 변환 실패", e);
        }
    }

    /**
     * DB 조회 시: JSON 문자열 → List<Double>
     * 예) "[0.123,0.456,...]" → [0.123, 0.456, ...]
     */
    @Override
    public List<Double> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<Double>>() {});
        } catch (IOException e) {
            log.error("JSON 문자열을 List<Double>로 역변환하는 중 오류 발생. 원본: {}", dbData, e);
            throw new IllegalArgumentException("JSON → List<Double> 변환 실패", e);
        }
    }
}
