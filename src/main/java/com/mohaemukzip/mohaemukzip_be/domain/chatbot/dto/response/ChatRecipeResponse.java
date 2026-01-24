package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "추천 레시피 카드 정보")
public class ChatRecipeResponse {
    
    @Schema(description = "레시피 ID", example = "101")
    private Long recipeId;

    @Schema(description = "레시피 제목", example = "얼큰 콩나물국")
    private String title;

    @Schema(description = "레시피 썸네일 URL", example = "https://s3.aws.com/image.jpg")
    private String imageUrl;
}
