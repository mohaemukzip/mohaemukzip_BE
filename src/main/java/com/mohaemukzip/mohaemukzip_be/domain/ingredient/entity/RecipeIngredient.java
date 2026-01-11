package com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity;

import com.mohaemukzip.mohaemukzip_be.domain.recipe.entity.Recipe;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "recipe_ingredients",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recipe_ingredient",
                        columnNames = {"recipe_id", "ingredient_id"}
                )
        })

public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_ingredient_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;
}
