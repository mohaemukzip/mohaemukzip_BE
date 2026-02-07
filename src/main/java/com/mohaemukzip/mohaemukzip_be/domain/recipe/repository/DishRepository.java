package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DishRepository extends JpaRepository<Dish, Long> {

    /**
     * 검색 로직 개선:
     * 1. 검색어 포함(LIKE %keyword%)하는 모든 결과 조회
     * 2. 정렬 우선순위:
     *    - 1순위: 검색어로 시작하는 경우 (CASE WHEN ... THEN 1)
     *    - 2순위: 그 외 (CASE WHEN ... THEN 2)
     *    - 3순위: 이름 길이 짧은 순 (LENGTH(d.name))
     *    - 4순위: 가나다 순 (d.name)
     */
    @Query("SELECT d.id as id, d.name as name FROM Dish d " +
           "WHERE d.name LIKE CONCAT('%', :keyword, '%') " +
           "ORDER BY " +
           "CASE WHEN d.name LIKE CONCAT(:keyword, '%') THEN 1 ELSE 2 END, " +
           "LENGTH(d.name), " +
           "d.name")
    Page<DishProjection> findProjectedByNameWeighted(@Param("keyword") String keyword, Pageable pageable);
}
