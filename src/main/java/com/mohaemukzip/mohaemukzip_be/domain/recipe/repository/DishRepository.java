package com.mohaemukzip.mohaemukzip_be.domain.recipe.repository;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DishRepository extends JpaRepository<Dish, Long> {

    @Query("SELECT d.id as id, d.name as name FROM Dish d WHERE REPLACE(d.name, ' ', '') LIKE CONCAT('%', :keyword, '%')")
    Page<DishProjection> findProjectedByNameContaining(@Param("keyword") String keyword, Pageable pageable);
}
