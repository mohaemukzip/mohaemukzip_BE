package com.mohaemukzip.mohaemukzip_be.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
//@Component
@RequiredArgsConstructor
// RecipeCrawler 테스트용
public class RecipeCrawlerRunner implements CommandLineRunner {

    private final RecipeCrawler crawler;
    private final IngredientRepository ingredientRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        // 1. 재료 이름 조회
        List<String> ingredientNames = ingredientRepository.findAllNames();
        log.info("✅ 재료 개수: {}", ingredientNames.size());

        // 2. 크롤링 실행
        String videoId = "j7s9VRsrm9o";  // 이 videoId만 내가 계속 직접 수정하면됨..

        log.info("🔍 크롤링 시작: {}", videoId);

        RecipeCrawler.RecipeData result = crawler.crawlRecipe(videoId, ingredientNames);

        // 3. JSON 출력
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(result);

        System.out.println("\n===== 크롤링 결과 =====");
        System.out.println(json);
        System.out.println("======================\n");
    }
}