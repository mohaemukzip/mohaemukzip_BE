package com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.*;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.crawler.RecipeCrawler;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class RecipeRegistrationPrecheckTest {
    @Mock RecipeCommandService commands;
    @Mock RecipeRepository recipes;
    @Mock DishRepository dishes;
    @Mock RecipeCrawler crawler;
    @InjectMocks RecipeAdminFacade facade;
    @InjectMocks RecipeCommandServiceImpl service;
    @Test void duplicateStopsBeforeExternalCalls() {
        doThrow(new BusinessException(ErrorStatus.RECIPE_ALREADY_EXISTS)).when(commands).validateRecipeRegistration(1L,"video");
        assertThatThrownBy(()->facade.saveRecipeByVideoId(1L,"video")).isInstanceOf(BusinessException.class);
        verifyNoInteractions(crawler); verify(commands,never()).getAllIngredientNames();
    }
    @Test void missingDishIsRejected() {
        assertThatThrownBy(()->service.validateRecipeRegistration(1L,"video"))
            .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.DISH_NOT_FOUND));
        verifyNoInteractions(recipes);
    }
    @Test void duplicateVideoIsRejected() {
        when(dishes.existsById(1L)).thenReturn(true); when(recipes.existsByVideoId("video")).thenReturn(true);
        assertThatThrownBy(()->service.validateRecipeRegistration(1L,"video"))
            .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.RECIPE_ALREADY_EXISTS));
    }
    @Test void invalidInputNeverQueriesDb() {
        assertThatThrownBy(()->service.validateRecipeRegistration(1L," ")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(()->service.validateRecipeRegistration(null,"video")).isInstanceOf(BusinessException.class);
        verifyNoInteractions(dishes,recipes);
    }
    @Test void validInputPreservesOrder() {
        when(commands.getAllIngredientNames()).thenReturn(List.of("양파"));
        when(commands.saveRecipeAndIngredients(1L,"video",null)).thenReturn(5L);
        assertThat(facade.saveRecipeByVideoId(1L,"video")).isEqualTo(5L);
        InOrder order=inOrder(commands,crawler);
        order.verify(commands).validateRecipeRegistration(1L,"video");
        order.verify(commands).getAllIngredientNames();
        order.verify(crawler).crawlRecipe("video",List.of("양파"));
        order.verify(commands).saveRecipeAndIngredients(1L,"video",null);
    }
    @Test void finalSaveStillRejectsDuplicate() {
        when(recipes.existsByVideoId("video")).thenReturn(true);
        assertThatThrownBy(()->service.saveRecipeAndIngredients(1L,"video",null)).isInstanceOf(BusinessException.class);
        verifyNoInteractions(crawler);
    }
}
