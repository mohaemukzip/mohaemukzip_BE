/**
 * findProjectedByTitleContaining 메서드는 Recipe 엔티티 전체가 아닌 id와 title 컬럼만 조회하도록 함.
 */


package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

public interface RecipeProjection {
    Long getId();
    String getTitle();
}
