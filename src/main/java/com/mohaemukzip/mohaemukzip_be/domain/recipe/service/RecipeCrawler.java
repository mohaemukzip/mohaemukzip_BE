package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.global.config.YouTubeConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RecipeCrawler {

    private final ObjectMapper objectMapper;
    private final YouTubeConfig youtubeConfig;
    private final WebClient youtubeWebClient;
    private final WebClient geminiRecipeWebClient;

    public RecipeCrawler(
            ObjectMapper objectMapper,
            YouTubeConfig youtubeConfig,
            @Qualifier("youtubeWebClient") WebClient youtubeWebClient,
            @Qualifier("geminiSummaryWebClient") WebClient geminiRecipeWebClient) {
        this.objectMapper = objectMapper;
        this.youtubeConfig = youtubeConfig;
        this.youtubeWebClient = youtubeWebClient;
        this.geminiRecipeWebClient = geminiRecipeWebClient;
    }

    @PostConstruct
    public void checkApiKeys() {
        log.info("YouTube API Key loaded: {}",
                youtubeConfig.getApiKey() != null && !youtubeConfig.getApiKey().isBlank());
        log.info("Gemini Recipe WebClient configured");
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

            String channelProfileImageUrl = null;
            try {
                channelProfileImageUrl = fetchChannelProfileImageUrl(youtubeData.channelId());
            } catch (Exception e) {
                // 채널 프로필은 부가 정보라 실패해도 전체 플로우를 깨지 않도록
                log.warn("채널 프로필 이미지 조회 실패 - channelId: {}, err: {}",
                        youtubeData.channelId(), e.getMessage());
            }

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
                    analysis.ingredients(),
                    channelProfileImageUrl
            );

            log.info("크롤링 성공 - videoId: {}", videoId);
            return result;

        } catch (WebClientResponseException.Unauthorized e) {
            log.error("API 인증 실패 - videoId: {}, 응답: {}", videoId, e.getResponseBodyAsString());
            throw new RuntimeException("API 인증 실패: API 키를 확인하세요", e);

        } catch (WebClientResponseException.Forbidden e) {
            log.error("API 할당량 초과 - videoId: {}, 응답: {}", videoId, e.getResponseBodyAsString());
            throw new RuntimeException("API 할당량 초과: 내일 다시 시도하세요", e);

        } catch (WebClientResponseException.NotFound e) {
            log.error("Gemini API 404 - endpoint/model/key 문제");
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
    private YouTubeData fetchYouTubeData(String videoId) {
        log.debug("📡 YouTube API 호출 - videoId: {}", videoId);

        String responseBody = youtubeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("part", "snippet,contentDetails,statistics")
                        .queryParam("id", videoId)
                        .queryParam("key", youtubeConfig.getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
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
        } catch (Exception e) {
            log.error("❌ YouTube API 응답 파싱 실패", e);
            throw new RuntimeException("YouTube API 응답 파싱 실패", e);
        }
    }

    /**
     * 채널 프로필 이미지 URL 조회
     */
    private String fetchChannelProfileImageUrl(String channelId) {
        if (channelId == null || channelId.isBlank()) {
            return null;
        }

        log.debug("📡 YouTube Channel API 호출 - channelId: {}", channelId);

        String responseBody = youtubeWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/channels")
                        .queryParam("part", "snippet")
                        .queryParam("id", channelId)
                        .queryParam("key", youtubeConfig.getApiKey())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode items = root.path("items");

            if (items.isEmpty()) {
                log.warn("⚠️ Channel not found - channelId: {}", channelId);
                return null;
            }

            JsonNode thumbnails = items.get(0).path("snippet").path("thumbnails");

            // 우선순위: high > medium > default
            String high = thumbnails.path("high").path("url").asText(null);
            if (high != null && !high.isBlank()) return high;

            String medium = thumbnails.path("medium").path("url").asText(null);
            if (medium != null && !medium.isBlank()) return medium;

            String def = thumbnails.path("default").path("url").asText(null);
            if (def != null && !def.isBlank()) return def;

            return null;
        } catch (Exception e) {
            log.error("❌ Channel API 응답 파싱 실패", e);
            return null;
        }
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
            log.warn("⚠️ Duration 파싱 실패: {}", isoDuration);
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
    ) {
        String prompt = buildPrompt(title, description, ingredientNames);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        log.debug("📡 Gemini API 호출 - title: {}", title);

        String responseBody = geminiRecipeWebClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (responseBody == null || responseBody.isBlank()) {
            throw new RuntimeException("Gemini API 응답 바디가 비어있습니다");
        }

        log.debug("Gemini raw response:\n{}", responseBody);

        try {
            JsonNode root = objectMapper.readTree(responseBody);

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

            // 코드블록 제거 (Gemini가 ```json ... ``` 로 감싸서 응답하는 경우)
            String cleanedJson = stripCodeBlock(rawText);
            log.debug("Gemini cleaned JSON:\n{}", cleanedJson);

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

        } catch (Exception e) {
            log.error("❌ Gemini 응답 파싱 실패", e);
            throw new RuntimeException("Gemini 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Gemini 응답에서 ```json ``` 코드블록 제거
     */
    private String stripCodeBlock(String text) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("Gemini API 응답 텍스트가 비어있습니다");
        }

        text = text.trim();

        // ```json ... ``` 또는 ``` ... ``` 제거
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*\n?", "");
            text = text.replaceFirst("\n?```$", "");
        }

        return text.trim();
    }

    /**
     * Gemini 프롬프트 생성
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
        설명, 마크다운, 코드블록(```) 없이 아래 JSON 스키마 그대로 반환하세요.
        
        다음 JSON 스키마 형식으로 반환하세요:
        {
          "category": "KOREAN",
          "cookingTime": 15,
          "ingredients": [
            {"name": "양배추", "amount": "1"},
            {"name": "소금", "amount": "0.5"}
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
            List<IngredientData> ingredients,
            String channelProfileImageUrl
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