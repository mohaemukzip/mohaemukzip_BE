package com.mohaemukzip.mohaemukzip_be.domain.Recipe.entity;

import com.mohaemukzip.mohaemukzip_be.domain.Member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "member_recipes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_recipe",
                        columnNames = {"member_id", "recipe_id"}
                )
        })

public class Member_recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_recipe_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;
}
