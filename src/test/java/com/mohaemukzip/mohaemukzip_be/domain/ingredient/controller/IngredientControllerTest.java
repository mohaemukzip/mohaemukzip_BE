package com.mohaemukzip.mohaemukzip_be.domain.ingredient.controller;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("키워드 + 카테고리 동시 검색 테스트")
    void searchByBoth() throws Exception {
        mockMvc.perform(get("/ingredients")
                        .param("query", "소")
                        .param("category", "MEAT_EGG"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("키워드 검색 테스트")
    void searchByKeywordOnly() throws Exception {
        mockMvc.perform(get("/ingredients")
                        .param("query", "당"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 검색 테스트")
    void searchByCategoryOnly() throws Exception {
        mockMvc.perform(get("/ingredients")
                        .param("category", "VEGETABLE"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("전체 조회 테스트")
    void searchByNone() throws Exception {
        mockMvc.perform(get("/ingredients"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}