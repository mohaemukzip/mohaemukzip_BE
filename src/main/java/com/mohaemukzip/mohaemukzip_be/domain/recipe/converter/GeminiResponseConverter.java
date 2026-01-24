package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiResponseConverter {

    private final ObjectMapper objectMapper;

    public List<StepDraft> convertToStepDrafts(String responseBody, int maxSteps) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new RuntimeException("Gemini 응답이 비어있습니다.");
        }

        JsonNode root = parseJson(responseBody, "Gemini 응답 파싱 실패");

        String rawText = extractTextFromResponse(root);
        String cleanedJson = stripCodeBlock(rawText);

        JsonNode resultNode = parseJson(cleanedJson, "Gemini JSON 파싱 실패");

        List<StepDraft> steps = extractSteps(resultNode);

        return limitSteps(steps, maxSteps);
    }

    private JsonNode parseJson(String json, String errorMessage) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("JSON 파싱 실패:\n{}", json);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private String extractTextFromResponse(JsonNode root) {
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            throw new RuntimeException("Gemini candidates 결과가 비어있습니다.");
        }

        JsonNode textNode = candidates.get(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode()) {
            throw new RuntimeException("Gemini text 결과가 비어있습니다.");
        }

        return textNode.asText();
    }

    private List<StepDraft> extractSteps(JsonNode resultNode) {
        JsonNode stepsNode = resultNode.path("steps");
        if (!stepsNode.isArray() || stepsNode.isEmpty()) {
            throw new RuntimeException("Gemini steps 결과가 비어있습니다.");
        }

        List<StepDraft> steps = new ArrayList<>();
        for (JsonNode node : stepsNode) {
            steps.add(new StepDraft(
                    node.path("stepNumber").asInt(),
                    node.path("title").asText(),
                    node.path("description").asText(),
                    node.path("videoTime").isMissingNode() || node.path("videoTime").isNull()
                            ? null
                            : node.path("videoTime").asInt()
            ));
        }
        return steps;
    }

    private List<StepDraft> limitSteps(List<StepDraft> steps, int maxSteps) {
        if (steps.size() > maxSteps) {
            return steps.subList(0, maxSteps);
        }
        return steps;
    }

    private String stripCodeBlock(String text) {
        if (text == null) return null;
        text = text.trim();

        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*", "");
            text = text.replaceFirst("```$", "");
        }
        return text.trim();
    }

    public record StepDraft(int stepNumber, String title, String description, Integer videoTime) {}
}
