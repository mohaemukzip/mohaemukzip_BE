package com.mohaemukzip.mohaemukzip_be.domain.recipe.converter;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.repository.RecipeRepository;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.command.RecipeCommandServiceImpl;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.service.crawler.RecipeCrawler;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeMetadataTest {
    @Mock RecipeRepository recipes;
    @Spy RecipeConverter converter=new RecipeConverter();
    @InjectMocks RecipeCommandServiceImpl commands;
    RecipeCrawler.RecipeData data(String title,String videoId,String url,String category,String profile) {
        return new RecipeCrawler.RecipeData(videoId,url,"channel",title,"description","thumbnail","author","10:00",5,10L,category,List.of(),profile);
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings={"   "})
    void missingChannelImageUsesEmptyUrl(String profile) {
        assertThat(converter.toEntity(data("요리","video","url","KOREAN",profile),null).getChannelProfileImageUrl()).isEmpty();
    }
    @Test void realProfileImageIsPreserved() {
        assertThat(converter.toEntity(data("요리","video","url","KOREAN","https://example.test/image"),null).getChannelProfileImageUrl()).isEqualTo("https://example.test/image");
    }
    @Test void requiredMetadataIsRejectedBeforeSave() {
        for (var d:List.of(data(" ","video","url","KOREAN",null),data("요리",null,"url","KOREAN",null),
                data("요리","video",null,"KOREAN",null),data("요리","video","url",null,null))) {
            assertThatThrownBy(()->commands.saveRecipeAndIngredients(null,"video",d))
                    .isInstanceOfSatisfying(BusinessException.class,e->assertThat(e.getBaseCode()).isEqualTo(ErrorStatus.EXTERNAL_API_ERROR));
        }
        verify(recipes,never()).save(any());
    }
    @Test void constraintFailureIsNotMislabelledDuplicateVideo() {
        var failure=new DataIntegrityViolationException("a different constraint");
        when(recipes.save(any())).thenThrow(failure);
        assertThatThrownBy(()->commands.saveRecipeAndIngredients(null,"video",data("요리","video","url","KOREAN",null))).isSameAs(failure);
    }
    @Test void nullProviderDataIsRejected() {
        assertThatThrownBy(()->converter.toEntity(null,null)).isInstanceOf(BusinessException.class);
    }
}
