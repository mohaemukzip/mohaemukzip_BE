package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.*;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.*;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.converter.*;
import com.mohaemukzip.mohaemukzip_be.global.client.transcript.TranscriptClient;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes=SummaryReservationTest.Config.class,webEnvironment=SpringBootTest.WebEnvironment.NONE,
    properties={"spring.datasource.url=jdbc:h2:mem:summary-reservation;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.database-platform=org.hibernate.dialect.H2Dialect","spring.jpa.show-sql=false"})
class SummaryReservationTest {
    @Configuration @EnableAutoConfiguration @EnableJpaAuditing
    @EntityScan("com.mohaemukzip.mohaemukzip_be.domain")
    @EnableJpaRepositories(basePackageClasses=RecipeRepository.class)
    static class Config {
        @Bean RecipeCommandServiceImpl commands(RecipeRepository r,RecipeStepRepository steps,SummaryRepository s) {
            return new RecipeCommandServiceImpl(r,null,null,null,null,steps,null,null,null,s,null,null,null,null,new RecipeStepConverter());
        }
    }
    @Autowired RecipeCommandServiceImpl commands;
    @Autowired RecipeRepository recipes;
    @Autowired SummaryRepository summaries;
    @Autowired RecipeStepRepository steps;
    @Autowired JdbcTemplate jdbc;
    Recipe recipe;
    @BeforeEach void fixture() {
        steps.deleteAll(); summaries.deleteAll(); recipes.deleteAll();
        recipe=recipes.saveAndFlush(Recipe.builder().title("요리").videoId("fixture-video").videoUrl("https://example.test/video").channelProfileImageUrl("image").build());
    }
    List<GeminiResponseConverter.StepDraft> drafts() {return List.of(new GeminiResponseConverter.StepDraft(1,"준비","재료 준비",0));}
    @Test void reservationIsNotCompleted() {
        commands.tryCreateSummary(recipe.getId());
        assertThatThrownBy(()->commands.getExistingSummaryResult(recipe.getId()))
            .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.SUMMARY_IN_PROGRESS));
        assertThat(recipes.findAllWithoutSummary(null,RecipeCommandServiceImpl.summaryReservationCutoff())).isEmpty();
    }
    @Test void completedSummaryIsReusable() {
        var s=commands.tryCreateSummary(recipe.getId());
        assertThat(commands.saveSummarySteps(s.getId(),drafts()).stepCount()).isEqualTo(1);
        assertThat(commands.tryCreateSummary(recipe.getId())).isNull();
        assertThat(commands.getExistingSummaryResult(recipe.getId()).summaryExists()).isTrue();
    }
    @Test void expiredEmptyReservationUsesNewIdAndRejectsOldWriter() {
        var old=commands.tryCreateSummary(recipe.getId());
        jdbc.update("update summaries set created_at=? where summary_id=?",LocalDateTime.now().minusMinutes(20),old.getId());
        assertThat(recipes.findAllWithoutSummary(null,RecipeCommandServiceImpl.summaryReservationCutoff())).hasSize(1);
        var current=commands.tryCreateSummary(recipe.getId());
        assertThat(current.getId()).isNotEqualTo(old.getId());
        assertThatThrownBy(()->commands.saveSummarySteps(old.getId(),drafts())).isInstanceOf(BusinessException.class);
        commands.deleteSummary(old.getId());
        assertThat(summaries.existsById(current.getId())).isTrue();
        assertThat(commands.saveSummarySteps(current.getId(),drafts()).stepCount()).isEqualTo(1);
    }
    @Test void duplicateRequestsReserveOnlyOnce() throws Exception {
        var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        try {
            Callable<Object> task=()->{start.await();try{return commands.tryCreateSummary(recipe.getId());}catch(BusinessException e){return e.getBaseCode();}};
            var a=pool.submit(task);var b=pool.submit(task);start.countDown();
            var results=List.of(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
            assertThat(results.stream().filter(Summary.class::isInstance)).hasSize(1);
            assertThat(results).contains(ErrorStatus.SUMMARY_IN_PROGRESS);
            assertThat(summaries.count()).isEqualTo(1);
        } finally {pool.shutdownNow();}
    }
    @Test void emptyStepsAreNeverCompleted() {
        var s=commands.tryCreateSummary(recipe.getId());
        assertThatThrownBy(()->commands.saveSummarySteps(s.getId(),List.of())).isInstanceOf(BusinessException.class);
        assertThat(steps.count()).isZero();
    }
    @Test void cleanupFailureKeepsOriginalFailure() {
        var service=mock(RecipeCommandService.class); var transcript=mock(TranscriptClient.class);
        var facade=new RecipeAdminFacade(service,mock(RecipeRepository.class),null,transcript,null,null,null);
        when(service.tryCreateSummary(1L)).thenReturn(Summary.builder().id(17L).build());
        when(service.getRecipeForSummary(1L)).thenReturn(Recipe.builder().videoId("video").build());
        var original=new IllegalStateException("provider failure");var cleanup=new IllegalStateException("cleanup failure");
        when(transcript.fetchTranscript("video")).thenThrow(original);doThrow(cleanup).when(service).deleteSummary(17L);
        assertThatThrownBy(()->facade.createSummary(1L)).isSameAs(original).satisfies(e->assertThat(e.getSuppressed()).containsExactly(cleanup));
    }
}
