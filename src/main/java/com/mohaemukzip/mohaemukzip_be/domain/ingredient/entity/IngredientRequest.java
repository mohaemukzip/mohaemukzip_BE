package com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity;


import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(
        name = "ingredient_request",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ingredient_request_member_name",
                columnNames = {"member_id", "ingredient_name"}
        ),
        indexes = {
                @Index(name = "idx_ingredient_request_member_name", columnList = "member_id, ingredient_name")
        }
        )
public class IngredientRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;
}
