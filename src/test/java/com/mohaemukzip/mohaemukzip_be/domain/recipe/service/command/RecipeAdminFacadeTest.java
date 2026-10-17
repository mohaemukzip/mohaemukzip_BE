package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.builder.GeminiPromptBuilder;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.GeminiResponseConverter;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Summary;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.crawler.RecipeCrawler;
import com.mohaemukzip.mohaemukzip_be.global.client.transcript.TranscriptClient;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeAdminFacadeTest {

    @Mock private RecipeCommandService recipeCommandService;
    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeCrawler recipeCrawler;
    @Mock private TranscriptClient transcriptClient;
    @Mock private GeminiPromptBuilder geminiPromptBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Recipe recipe = Recipe.builder().id(7L).title("김치찌개").videoId("sample-video").build();
        when(recipeCommandService.tryCreateSummary(7L)).thenReturn(Summary.builder().id(17L).recipe(recipe).build());
        when(recipeCommandService.getRecipeForSummary(7L)).thenReturn(recipe);
        when(transcriptClient.fetchTranscript("sample-video")).thenReturn(List.of());
        when(geminiPromptBuilder.buildRecipeStepPrompt("김치찌개", List.of())).thenReturn("요약 프롬프트");
    }

    private RecipeAdminFacade facadeWithResponse(String text) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("candidates", List.of(
                Map.of("content", Map.of("parts", List.of(Map.of("text", text)))))));
        WebClient webClient = WebClient.builder().baseUrl("https://example.invalid")
                .exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE).body(body).build()))
                .build();

        return new RecipeAdminFacade(recipeCommandService, recipeRepository, recipeCrawler, transcriptClient,
                geminiPromptBuilder, new GeminiResponseConverter(objectMapper), webClient);
    }

    @Test
    void rejectsEntireSummaryAndCleansReservationWhenAnyStepIsInvalid() throws Exception {
        RecipeAdminFacade facade = facadeWithResponse("""
                {"steps":[
                  {"stepNumber":1,"title":"준비","description":"재료 준비","videoTime":0},
                  {}
                ]}
                """);

        assertThatThrownBy(() -> facade.createSummary(7L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getBaseCode())
                .isEqualTo(ErrorStatus.SUMMARY_CREATION_FAILED);
        verify(recipeCommandService, never()).saveSummarySteps(anyLong(), anyList());
        verify(recipeCommandService).deleteSummary(17L);
    }

    @Test
    void savesValidSummaryWithoutDeletingReservation() throws Exception {
        RecipeAdminFacade facade = facadeWithResponse("""
                {"steps":[{"stepNumber":1,"title":"준비","description":"재료 준비","videoTime":0}]}
                """);
        RecipeResponseDTO.SummaryCreateResult expected = new RecipeResponseDTO.SummaryCreateResult(true, 1);
        when(recipeCommandService.saveSummarySteps(eq(17L), anyList())).thenReturn(expected);

        assertThat(facade.createSummary(7L)).isEqualTo(expected);
        verify(recipeCommandService).saveSummarySteps(17L,
                List.of(new GeminiResponseConverter.StepDraft(1, "준비", "재료 준비", 0)));
        verify(recipeCommandService, never()).deleteSummary(anyLong());
    }
}
