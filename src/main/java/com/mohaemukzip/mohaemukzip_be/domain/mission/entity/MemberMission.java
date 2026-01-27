package com.mohaemukzip.mohaemukzip_be.domain.mission.entity;

import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.enums.MissionStatus;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "member_missions", uniqueConstraints = {
        @UniqueConstraint(
                name = "UQ_MEMBER_ASSIGNED_DATE",
                columnNames = {"member_id", "assigned_date"}
        )
})
public class MemberMission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_mission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "assigned_date",  nullable = false)
    private LocalDate assignedDate; // 오늘의 퀘스트 날짜

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MissionStatus status = MissionStatus.ASSIGNED;


    public void completeToday() {
        this.status = MissionStatus.COMPLETED;
    }

    public void failToday() {
        this.status = MissionStatus.FAILED;
    }

}
