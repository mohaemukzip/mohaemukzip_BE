package com.mohaemukzip.mohaemukzip_be.domain.Ingredient.entity;

import com.mohaemukzip.mohaemukzip_be.domain.Ingredient.entity.enums.StorageType;
import com.mohaemukzip.mohaemukzip_be.domain.Member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "member_ingredients")
public class Member_Ingredient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_ingredient_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "expire_date")
    private LocalDate expireDate;

    @Column(name = "storage_type")
    private StorageType storageType;

    @Column(name = "weight")
    private Double weight;
}
