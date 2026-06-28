package com.mohaemukzip.mohaemukzip_be.domain.recipe.service;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.RecipeEmbeddingService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 1회성 임베딩 데이터 세팅을 위한 테스트 클래스.
 * 웹(Controller)을 거치지 않아 Spring Security(401) 등 권한 문제를 우회하여
 * DB에 초기 임베딩 데이터를 밀어넣을 때 사용합니다.
 */
@Disabled("수동 1회성 임베딩 배치 실행용")
@SpringBootTest
public class RecipeEmbeddingServiceTest {

    @Autowired
    private RecipeEmbeddingService recipeEmbeddingService;

    @Test
    @DisplayName("DB에 임베딩이 없는 모든 레시피에 대해 임베딩을 생성하고 저장한다")
    void embedAllMissingRecipes() {
        System.out.println("==================================================");
        System.out.println("🚀 [1회성 배치 작업] 임베딩 생성 테스트 시작");
        System.out.println("==================================================");

        // RecipeEmbeddingService에 구현된 메서드 호출
        String result = recipeEmbeddingService.generateMissingEmbeddings();

        System.out.println("==================================================");
        System.out.println("🎉 [1회성 배치 작업] 완료 결과 요약");
        System.out.println("👉 " + result);
        System.out.println("==================================================");
    }
}
