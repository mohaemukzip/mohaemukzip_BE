package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RAG 챗봇이 반환하는 레시피 카드 DTO.
 * Gemini가 JSON 배열로 응답한 내용을 파싱하여 채워집니다.
 * 프론트엔드에서 카드 형태(recipe_id, title, recommend_reason, ingredients_match_rate)로 렌더링됩니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "RAG 추천 레시피 카드 정보")
public class RecipeCardResponse {

    @JsonProperty("recipe_id")
    @Schema(description = "레시피 DB ID", example = "2")
    private Long recipeId;

    @Schema(description = "레시피 제목", example = "1등🥇 식당처럼 맛있게 [돼지고기 김치찌개] 끓이는법! 한 숟갈만 드셔도 극찬이~ 실패는 없다!")
    private String title;

    @JsonProperty("recommend_reason")
    @Schema(description = "AI가 생성한 추천 이유 (2~3문장)", example = "등록된 냉장고 재료나 최근 식사 이력이 없어 비 오는 날의 일반적인 선호도를 고려했습니다. 비 오는 날 따뜻하고 칼칼한 국물이 생각날 때, 돼지고기 김치찌개는 몸과 마음을 녹여줄 완벽한 선택입니다.")
    private String recommendReason;

    @JsonProperty("ingredients_match_rate")
    @Schema(description = "냉장고 재료 매칭률 (0~100)", example = "75")
    private Integer ingredientsMatchRate;
}
