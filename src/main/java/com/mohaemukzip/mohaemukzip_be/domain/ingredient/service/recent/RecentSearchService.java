package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service.recent;

import java.util.List;

public interface RecentSearchService {

    // 최근 검색어 저장
    void saveRecentSearch(Long memberId, String keyword);

    // 최근 검색어 목록 조회
    List<String> getRecentSearches(Long memberId);

    //특정 최근 검색어 삭제
    void deleteRecentSearch(Long memberId, String keyword);
}
