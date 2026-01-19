package com.mohaemukzip.mohaemukzip_be.domain.search.controller;

import com.mohaemukzip.mohaemukzip_be.domain.search.dto.SearchResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.search.service.SearchService;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search/keywords")
@Tag(name = "Search")
@Validated
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(summary = "통합 검색 API", description = "재료 및 레시피(메뉴)를 통합 검색합니다.")
    public ApiResponse<SearchResponseDTO> search(
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") @Size(min = 1, max = 100, message = "검색어는 1자 이상 100자 이하로 입력해주세요.") String keyword) {
        return ApiResponse.onSuccess(searchService.search(keyword));
    }
}
