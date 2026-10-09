package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiResponseConverterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeminiResponseConverter converter = new GeminiResponseConverter(objectMapper);

    private String response(String text) throws Exception {
        return objectMapper.writeValueAsString(Map.of("candidates", List.of(
                Map.of("content", Map.of("parts", List.of(Map.of("text", text)))))));
    }

    private static String validStep(int number) {
        return "{\"stepNumber\":" + number
                + ",\"title\":\"준비\",\"description\":\"재료 준비\",\"videoTime\":12}";
    }

    @Test
    void preservesValidStepsAndOptionalVideoTimes() throws Exception {
        String text = """
                {"steps":[
                  {"stepNumber":1,"title":" 준비 ","description":"재료 준비","videoTime":0},
                  {"stepNumber":2,"title":"조리","description":"재료 조리"},
                  {"stepNumber":3,"title":"마무리","description":"접시에 담기","videoTime":null}
                ]}
                """;

        assertThat(converter.convertToStepDrafts(response(text), 10)).containsExactly(
                new GeminiResponseConverter.StepDraft(1, " 준비 ", "재료 준비", 0),
                new GeminiResponseConverter.StepDraft(2, "조리", "재료 조리", null),
                new GeminiResponseConverter.StepDraft(3, "마무리", "접시에 담기", null));
    }

    @Test
    void acceptsValidJsonInsideMarkdownCodeBlock() throws Exception {
        String text = "```json\n{\"steps\":[" + validStep(1) + "]}\n```";

        assertThat(converter.convertToStepDrafts(response(text), 10))
                .containsExactly(new GeminiResponseConverter.StepDraft(1, "준비", "재료 준비", 12));
    }

    @Test
    void retainsExistingTenStepLimitForValidInput() throws Exception {
        String steps = IntStream.rangeClosed(1, 11).mapToObj(GeminiResponseConverterTest::validStep)
                .collect(Collectors.joining(",", "{\"steps\":[", "]}"));

        assertThat(converter.convertToStepDrafts(response(steps), 10))
                .extracting(GeminiResponseConverter.StepDraft::stepNumber)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    @Test
    void rejectsInvalidStepEvenBeyondOutputLimit() throws Exception {
        String firstTen = IntStream.rangeClosed(1, 10).mapToObj(GeminiResponseConverterTest::validStep)
                .collect(Collectors.joining(","));
        String text = "{\"steps\":[" + firstTen + ",{}]}";

        assertThatThrownBy(() -> converter.convertToStepDrafts(response(text), 10))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getBaseCode())
                .isEqualTo(ErrorStatus.SUMMARY_CREATION_FAILED);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidStepLists")
    void rejectsInvalidStepValuesWithoutCoercion(String scenario, String steps) throws Exception {
        String body = response("{\"steps\":" + steps + "}");

        assertThatThrownBy(() -> converter.convertToStepDrafts(body, 10))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getBaseCode())
                .isEqualTo(ErrorStatus.SUMMARY_CREATION_FAILED);
    }

    private static Stream<Arguments> invalidStepLists() {
        String valid = validStep(1);
        return Stream.of(
                Arguments.of("빈 steps", "[]"),
                Arguments.of("배열이 아닌 steps", "{}"),
                Arguments.of("null steps", "null"),
                Arguments.of("null 단계", "[null]"),
                Arguments.of("필수 값 없는 단계", "[{}]"),
                Arguments.of("문자열 번호", "[" + valid.replace("\"stepNumber\":1", "\"stepNumber\":\"1\"") + "]"),
                Arguments.of("소수 번호", "[" + valid.replace("\"stepNumber\":1", "\"stepNumber\":1.5") + "]"),
                Arguments.of("불리언 번호", "[" + valid.replace("\"stepNumber\":1", "\"stepNumber\":true") + "]"),
                Arguments.of("0번 단계", "[" + validStep(0) + "]"),
                Arguments.of("음수 번호", "[" + validStep(-1) + "]"),
                Arguments.of("번호 int 범위 초과", "[" + valid.replace("\"stepNumber\":1", "\"stepNumber\":2147483648") + "]"),
                Arguments.of("중복 번호", "[" + validStep(1) + "," + validStep(1) + "]"),
                Arguments.of("빠진 번호", "[" + validStep(1) + "," + validStep(3) + "]"),
                Arguments.of("뒤바뀐 번호", "[" + validStep(2) + "," + validStep(1) + "]"),
                Arguments.of("빈 제목", "[" + valid.replace("\"title\":\"준비\"", "\"title\":\"\"") + "]"),
                Arguments.of("공백 제목", "[" + valid.replace("\"title\":\"준비\"", "\"title\":\"   \"") + "]"),
                Arguments.of("숫자 제목", "[" + valid.replace("\"title\":\"준비\"", "\"title\":42") + "]"),
                Arguments.of("null 제목", "[" + valid.replace("\"title\":\"준비\"", "\"title\":null") + "]"),
                Arguments.of("설명 누락", "[" + valid.replace(",\"description\":\"재료 준비\"", "") + "]"),
                Arguments.of("공백 설명", "[" + valid.replace("\"description\":\"재료 준비\"", "\"description\":\"  \"") + "]"),
                Arguments.of("배열 설명", "[" + valid.replace("\"description\":\"재료 준비\"", "\"description\":[]") + "]"),
                Arguments.of("음수 시간", "[" + valid.replace("\"videoTime\":12", "\"videoTime\":-1") + "]"),
                Arguments.of("문자열 시간", "[" + valid.replace("\"videoTime\":12", "\"videoTime\":\"12\"") + "]"),
                Arguments.of("소수 시간", "[" + valid.replace("\"videoTime\":12", "\"videoTime\":12.5") + "]"),
                Arguments.of("불리언 시간", "[" + valid.replace("\"videoTime\":12", "\"videoTime\":false") + "]"),
                Arguments.of("시간 int 범위 초과", "[" + valid.replace("\"videoTime\":12", "\"videoTime\":2147483648") + "]"),
                Arguments.of("정상 단계 뒤 불량 단계", "[" + validStep(1) + ",{}]"));
    }
}
