package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.controller.AdminRecipeController;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.global.client.DiscordNotificationClient;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import java.util.*;
import java.util.stream.IntStream;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecipeBatchTest {
    final String id="abcdefghijk";
    @Test void dedupKeepsFirstOrderAndCopiesInput() {
        var input=new ArrayList<>(List.of(id,"12345678901",id));
        var result=RecipeBatchPolicy.normalize(input);input.clear();
        assertThat(result).containsExactly(id,"12345678901");
    }
    @Test void rejectsInvalidItemsAndLimitsRawInput() {
        for (var input:List.of(List.<String>of(),List.of(" "),List.of("short"),Collections.singletonList((String)null),Collections.nCopies(51,id))) {
            assertThatThrownBy(()->RecipeBatchPolicy.normalize(input)).isInstanceOf(RuntimeException.class);
        }
        assertThatThrownBy(()->RecipeBatchPolicy.normalize(null)).isInstanceOf(RuntimeException.class);
        assertThat(RecipeBatchPolicy.normalize(Collections.nCopies(50,id))).containsExactly(id);
    }
    @Test void requestBeanValidationRejectsItemsAndOversizedLists() {
        try (var factory=Validation.buildDefaultValidatorFactory()) {
            var v=factory.getValidator();
            assertThat(v.validate(new AdminRecipeController.BulkRecipeRequest(1L,List.of("bad")))).isNotEmpty();
            assertThat(v.validate(new AdminRecipeController.BulkRecipeRequest(1L,Collections.nCopies(51,id)))).isNotEmpty();
            assertThat(v.validate(new AdminRecipeController.BulkRecipeRequest(1L,List.of(id)))).isEmpty();
        }
    }
    @Test void controllerDeduplicatesBeforeAsyncSubmission() {
        var async=mock(AdminRecipeService.class);
        var controller=new AdminRecipeController(null,async);
        controller.createRecipesInBulk(new AdminRecipeController.BulkRecipeRequest(1L,List.of(id,id)));
        verify(async).processBulkRecipesAsync(1L,List.of(id));
    }
    @Test void incompleteSummaryIsFailureAndDuplicateIsOnlyAttemptedOnce() {
        var facade=mock(RecipeAdminFacade.class);var discord=mock(DiscordNotificationClient.class);
        when(facade.saveRecipeByVideoId(1L,id)).thenReturn(10L);
        when(facade.createSummary(10L)).thenReturn(RecipeResponseDTO.SummaryCreateResult.builder().summaryExists(true).stepCount(0).build());
        new AdminRecipeService(facade,discord).processBulkRecipesAsync(1L,List.of(id,id));
        verify(facade,times(1)).saveRecipeByVideoId(1L,id);
        verify(discord).sendNotification(argThat(m->m.contains("성공: 0건")&&m.contains("실패: 1건")));
    }
    @Test void alertFailureDoesNotRerunCompletedWork() {
        var facade=mock(RecipeAdminFacade.class);var discord=mock(DiscordNotificationClient.class);
        when(facade.saveRecipeByVideoId(1L,id)).thenReturn(10L);
        when(facade.createSummary(10L)).thenReturn(RecipeResponseDTO.SummaryCreateResult.builder().summaryExists(true).stepCount(1).build());
        doThrow(new IllegalStateException("alert")).when(discord).sendNotification(anyString());
        assertThatCode(()->new AdminRecipeService(facade,discord).processBulkRecipesAsync(1L,List.of(id))).doesNotThrowAnyException();
        verify(facade,times(1)).createSummary(10L);
    }
    @Test void manyFailuresStayWithinDiscordLimit() {
        var facade=mock(RecipeAdminFacade.class);var discord=mock(DiscordNotificationClient.class);
        when(facade.saveRecipeByVideoId(eq(1L),anyString())).thenThrow(new IllegalStateException("오류".repeat(3000)));
        var ids=IntStream.range(0,50).mapToObj(n->String.format("%011d",n)).toList();
        new AdminRecipeService(facade,discord).processBulkRecipesAsync(1L,ids);
        verify(discord).sendNotification(argThat(m->m.length()<=2000&&m.contains("실패: 50건")));
    }
    @Test void truncationDoesNotSplitSurrogate() {
        assertThat(RecipeBatchPolicy.truncate("🍳".repeat(2000),2000)).hasSizeLessThanOrEqualTo(2000);
        var result=RecipeBatchPolicy.truncate("a".repeat(1998)+"🍳x",2000);
        assertThat(result).endsWith("…");
        assertThat(Character.isHighSurrogate(result.charAt(result.length()-2))).isFalse();
    }
}
