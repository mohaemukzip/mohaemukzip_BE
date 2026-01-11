package com.mohaemukzip.mohaemukzip_be.domain.Mission.entity;

import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "missions")
public class Mission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "reward")
    private Integer reward;

    @Column(name = "explain", nullable = false, columnDefinition = "TEXT")
    private String explain;
}
