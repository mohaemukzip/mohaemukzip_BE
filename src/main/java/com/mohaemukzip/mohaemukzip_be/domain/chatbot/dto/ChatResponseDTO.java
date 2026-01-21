package com.mohaemukzip.mohaemukzip_be.domain.chatbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mohaemukzip.mohaemukzip_be.domain.chatbot.entity.enums.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponseDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "챗봇 응답 결과 DTO")
    public static class ChatResultDto {
        
        @Schema(description = "메시지 ID", example = "10")
        private Long id;

        @Schema(description = "발신자 타입 (USER/BOT)", example = "BOT")
        private SenderType senderType;

        @Schema(description = "챗봇 응답 메시지", example = "유통기한이 임박한 콩나물로 만들 수 있는 콩나물국 레시피를 찾아봤어요!")
        private String message;

        @Schema(description = "메시지 생성 시간 (ISO 8601)", example = "2023-10-27T14:30:00")
        private LocalDateTime createdAt;

        @Schema(description = "화면 표시용 시간 포맷", example = "오후 2:30")
        private String formattedTime;
        
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "추천 레시피 목록 (추천이 없을 경우 null)")
        private List<RecipeCardDto> recommendRecipes;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "추천 레시피 카드 정보")
    public static class RecipeCardDto {
        
        @Schema(description = "레시피 ID", example = "101")
        private Long recipeId;

        @Schema(description = "레시피 제목", example = "얼큰 콩나물국")
        private String title;

        @Schema(description = "레시피 썸네일 URL", example = "https://s3.aws.com/image.jpg")
        private String imageUrl;
    }
}
