package com.mohaemukzip.mohaemukzip_be.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@Slf4j
@Component
public class RecipeCrawler {
    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public RecipeCrawler(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }


    @PostConstruct
    public void checkApiKeys() {
        log.info(" YouTube API Key loaded: {}", youtubeApiKey != null && !youtubeApiKey.isBlank());
        log.info(" Gemini API Key loaded: {}", geminiApiKey != null && !geminiApiKey.isBlank());
    }


    /**
     * YouTube Video ID로 레시피 정보 + 재료 추출
     */
    public RecipeData crawlRecipe(String videoId, List<String> ingredientNames) {
        log.info("크롤링 시작 - videoId: {}", videoId);

        try {
            // 1. YouTube Data API 호출
            YouTubeData youtubeData = fetchYouTubeData(videoId);
            log.info("YouTube 데이터 조회 성공 - title: {}", youtubeData.title());

            // 2. Gemini API로 카테고리 + 조리시간 + 재료 추출
            RecipeAnalysis analysis = extractRecipeData(
                    youtubeData.title(),
                    youtubeData.description(),
                    ingredientNames
            );
            log.info("Gemini 분석 완료 - category: {}, ingredients: {}",
                    analysis.category(), analysis.ingredients().size());

            // 3. videoUrl 생성
            String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

            // 4. 결과 조합
            RecipeData result = new RecipeData(
                    youtubeData.videoId(),
                    videoUrl,
                    youtubeData.channelId(),
                    youtubeData.title(),
                    youtubeData.description(),
                    youtubeData.thumbnailUrl(),
                    youtubeData.channelTitle(),
                    youtubeData.time(),
                    analysis.cookingTime(),
                    youtubeData.viewCount(),
                    analysis.category(),
                    analysis.ingredients()
            );

            log.info("크롤링 성공 - videoId: {}", videoId);
            return result;

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("API 인증 실패 - videoId: {}, 응답: {}", videoId, e.getResponseBodyAsString());
            throw new RuntimeException("API 인증 실패: API 키를 확인하세요", e);

        } catch (HttpClientErrorException.Forbidden e) {
            log.error("API 할당량 초과 - videoId: {}, 응답: {}", videoId, e.getResponseBodyAsString());
            throw new RuntimeException("API 할당량 초과: 내일 다시 시도하세요", e);

        } catch (HttpClientErrorException.NotFound e) {
            log.error("❌ Gemini API 404 - endpoint/model/key 문제");
            log.error("응답 바디: {}", e.getResponseBodyAsString());
            throw e;

        } catch (Exception e) {
            log.error("크롤링 실패 - videoId: {}, 에러: {}", videoId, e.getMessage(), e);
            throw new RuntimeException("크롤링 실패: " + e.getMessage(), e);
        }
    }

    /**
     * YouTube Data API v3 호출
     */
    private YouTubeData fetchYouTubeData(String videoId) throws Exception {
        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
                .queryParam("part", "snippet,contentDetails,statistics")
                .queryParam("id", videoId)
                .queryParam("key", youtubeApiKey)
                .build(true)
                .toUri();

        log.debug("YouTube API 호출 - videoId: {}", videoId);

        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode items = root.path("items");

        if (items.isEmpty()) {
            throw new RuntimeException("Video not found: " + videoId);
        }

        JsonNode item = items.get(0);
        JsonNode snippet = item.path("snippet");
        JsonNode contentDetails = item.path("contentDetails");
        JsonNode statistics = item.path("statistics");

        // Duration 파싱 (ISO 8601 → MM:SS)
        String isoDuration = contentDetails.path("duration").asText();
        String formattedTime = parseDuration(isoDuration);

        return new YouTubeData(
                videoId,
                snippet.path("channelId").asText(),
                snippet.path("title").asText(),
                snippet.path("description").asText(),
                snippet.path("thumbnails").path("medium").path("url").asText(),
                snippet.path("channelTitle").asText(),
                formattedTime,
                statistics.path("viewCount").asLong()
        );
    }

    /**
     * ISO 8601 Duration → MM:SS 변환
     * ex) PT10M54S → "10:54"
     */
    private String parseDuration(String isoDuration) {
        try {
            Duration duration = Duration.parse(isoDuration);
            long totalSeconds = duration.getSeconds();

            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;

            return String.format("%d:%02d", minutes, seconds);
        } catch (Exception e) {
            log.warn("Duration 파싱 실패: {}", isoDuration);
            return "0:00";
        }
    }

    /**
     * Gemini API로 카테고리 + 조리시간 + 재료 추출
     */
    private RecipeAnalysis extractRecipeData(
            String title,
            String description,
            List<String> ingredientNames
    ) throws Exception {

        String prompt = buildPrompt(title, description, ingredientNames);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        String url = String.format(
                "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=%s",
                geminiApiKey
        );

        log.debug("Gemini API 호출 - title: {}", title);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        log.info("Gemini status: {}", response.getStatusCode());
        log.debug("Gemini raw body:\n{}", response.getBody());

        // 응답 파싱
        JsonNode root = objectMapper.readTree(response.getBody());

        JsonNode candidates = root.path("candidates");
        if (candidates.isEmpty() || candidates.get(0) == null) {
            throw new RuntimeException("Gemini API 응답에 candidates가 없습니다");
        }
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (parts.isEmpty() || parts.get(0) == null) {
            throw new RuntimeException("Gemini API 응답에 parts가 없습니다");
        }
        String rawText = parts.get(0).path("text").asText();

        log.debug("Gemini raw text:\n{}", rawText);

        // 여기서 코드블록 제거 (자꾸 Gemini가 ''' 내보냄)
        String cleanedJson = stripCodeBlock(rawText);
        log.debug("Gemini cleaned json:\n{}", cleanedJson);


        JsonNode resultNode = objectMapper.readTree(cleanedJson);

        String category = resultNode.path("category").asText();
        Integer cookingTime = resultNode.path("cookingTime").asInt();
        JsonNode ingredientsNode = resultNode.path("ingredients");
        if (!ingredientsNode.isArray()) {
            throw new RuntimeException("Gemini 응답의 ingredients가 배열이 아닙니다");
        }

        List<IngredientData> ingredients = new ArrayList<>();
        for (JsonNode node : ingredientsNode) {
            ingredients.add(new IngredientData(
                    node.path("name").asText(),
                    node.path("amount").asText()
            ));
        }

        return new RecipeAnalysis(category, cookingTime, ingredients);
    }

    /**
     * Gemini 응답에서 ```json ``` 코드블록 제거
     */
    private String stripCodeBlock(String text) {
        if (text == null) return null;

        text = text.trim();

        // ```json ... ``` 또는 ``` ... ```
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*", "");
            text = text.replaceFirst("```$", "");
        }

        return text.trim();
    }

    /**
     * Gemini 프롬프트
     */
    private String buildPrompt(String title, String description, List<String> ingredientNames) {
        String ingredientList = (ingredientNames == null) ? "" : String.join(", ", ingredientNames);

        return String.format("""
        다음은 우리 시스템의 재료 목록입니다.
        이 목록은 정규화된 값이며, 새로운 재료를 생성해서는 안 됩니다.
        
        재료 목록:
        %s
        
        레시피 제목: %s
        레시피 설명: %s
        
        위 레시피 정보를 분석하여 다음을 추출해주세요:
        
        1. 카테고리(category): 다음 중 하나를 선택하세요
           - KOREAN: 한식 (김치찌개, 제육볶음, 된장찌개 등)
           - CHINESE: 중식 (마파두부, 짜장면, 깐풍기 등)
           - JAPANESE: 일식 (초밥, 돈카츠, 우동 등)
           - WESTERN: 양식 (파스타, 스테이크, 피자 등)
           - ASIAN: 아시아식 (쌀국수, 카레, 팟타이 등)
        
        2. 조리 시간(cookingTime): 실제 요리하는데 걸리는 시간 (분 단위, 정수)
           - 레시피 설명이나 제목에서 조리 시간 추정
           - 명시되지 않았다면 재료와 조리 방법을 고려하여 예측
           - 예: 제육볶음 → 15분, 김치찌개 → 20분, 파스타 → 25분
        
        3. 재료(ingredients):
           - 반드시 위에 제공된 재료 목록에서만 선택하세요.
           - ingredients[].name 값은 **재료 목록에 있는 문자열 중 하나와 정확히 동일해야 합니다.**
           - 재료 목록에 없는 이름을 생성하거나 변형해서는 안 됩니다.
           - 유사한 재료가 있을 경우에도, 반드시 목록에 존재하는 가장 적절한 하나를 선택하세요.
           - 만약 어떤 재료도 적절하지 않다면 해당 재료는 제외하세요.
        
           분량 규칙:
           - 숫자만 반환 (단위 제외)
           - "200g" → "200"
           - "1개" → "1"
           - "반 개" → "0.5"
           - "한 줌" → "1"
           - "두 줌" → "2"
           - "적당량" → "1"
           - "약간" → "0.5"
           - "넉넉히" → "2"
           - 분량이 전혀 없으면 → "1" (기본값)
           - 모든 재료는 반드시 숫자 분량을 가져야 합니다
        
        
        반드시 JSON만 출력하세요.
            설명, 마크다운, 코드블록(```) 없이
            아래 JSON 스키마 그대로 반환하세요.
        
        amount는 숫자(number) 타입으로 반환하세요 (문자열 X)
        
        다음 JSON 스키마 형식으로 반환하세요:
        {
          "category": "KOREAN",
          "cookingTime": 15,
          "ingredients": [
            {"name": "양배추", "amount": 1},
            {"name": "소금", "amount": 0.5}
          ]
        }
        
        출력은 반드시 JSON 객체 하나여야 하며,
        첫 글자는 { 로 시작하고 마지막 글자는 } 로 끝나야 합니다.
        """, ingredientList, title, description);
    }

// ===== Record DTOs =====

    /**
     * 크롤링 최종 결과
     */
    public record RecipeData(
            String videoId,
            String videoUrl,
            String channelId,
            String title,
            String description,
            String thumbnailUrl,
            String channelTitle,
            String time,           // "10:54" (영상 길이)
            Integer cookingTime,   // 15 (조리 시간)
            Long viewCount,
            String category,       // "KOREAN", "CHINESE" 등
            List<IngredientData> ingredients
    ) {}

    /**
     * YouTube API 응답
     */
    private record YouTubeData(
            String videoId,
            String channelId,
            String title,
            String description,
            String thumbnailUrl,
            String channelTitle,
            String time,
            Long viewCount
    ) {}

    /**
     * Gemini API 분석 결과
     */
    private record RecipeAnalysis(
            String category,
            Integer cookingTime,
            List<IngredientData> ingredients
    ) {}

    /**
     * 재료 데이터
     */
    public record IngredientData(
            String name,
            String amount
    ) {}
}
