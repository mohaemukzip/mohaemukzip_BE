package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DishRepository extends JpaRepository<Dish, Long> {


    // 주의: 첫 글자만 탐색하기 때문에 DB에 저장된 Dish 이름에 띄어쓰기가 있다면 검색되지 않을 수 있음 (예: DB '제 육 볶음' vs 검색어 '제육')
    @Query("SELECT d.id as id, d.name as name FROM Dish d WHERE d.name LIKE CONCAT(:keyword, '%')")
    Page<DishProjection> findProjectedByNameStartingWith(@Param("keyword") String keyword, Pageable pageable);
}
