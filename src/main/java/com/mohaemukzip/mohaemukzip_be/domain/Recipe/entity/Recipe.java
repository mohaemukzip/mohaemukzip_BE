package com.mohaemukzip.mohaemukzip_be.domain.Recipe.entity;

import com.mohaemukzip.mohaemukzip_be.domain.Recipe.entity.enums.Category;
import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "recipes")
public class Recipe extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "level")
    private Integer level;

    @Column(name = "time")
    private Integer time;

    @Column(name = "channel")
    private String channel;

    @Column(name = "views")
    private Integer views;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Category category;
}