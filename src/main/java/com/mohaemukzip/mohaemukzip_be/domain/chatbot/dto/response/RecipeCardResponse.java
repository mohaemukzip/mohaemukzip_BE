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
    @Schema(description = "레시피 DB ID", example = "42")
    private Long recipeId;

    @Schema(description = "레시피 제목", example = "매콤 두부 조림")
    private String title;

    @JsonProperty("recommend_reason")
    @Schema(description = "AI가 생성한 추천 이유 (2~3문장)", example = "냉장고에 있는 두부를 활용할 수 있어요! 최근 드신 메뉴와 겹치지 않아 딱 좋습니다.")
    private String recommendReason;

    @JsonProperty("ingredients_match_rate")
    @Schema(description = "냉장고 재료 매칭률 (0~100)", example = "75")
    private Integer ingredientsMatchRate;
}
