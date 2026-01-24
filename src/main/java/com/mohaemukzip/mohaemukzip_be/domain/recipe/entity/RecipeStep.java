package com.mohaemukzip.mohaemukzip_be.domain.recipe.entity;

import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "recipe_steps",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_summary_step", columnNames = {"summary_id", "step_number"})
        })
public class RecipeStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_step_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "summary_id", nullable = false)
    private Summary summary;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "title", nullable = false)
    private String title;        // "고기와 기본 재료 준비하기"

    @Column(name = "description", length = 1000, nullable = false)
    private String description;  // "돼지고기 앞다리살을 썰고, 기본 채소들을 씻어줍니다"

    @Column(name = "video_time")
    private Integer videoTime; // timestamp 초 단위 기준
}
