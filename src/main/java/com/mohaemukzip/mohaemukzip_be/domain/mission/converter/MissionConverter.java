package com.mohaemukzip.mohaemukzip_be.domain.mission.converter;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.MemberMission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.Mission;
import com.mohaemukzip.mohaemukzip_be.domain.mission.entity.enums.MissionStatus;

import java.time.LocalDate;

public class MissionConverter {

    public static MemberMission toMemberMission(Long memberId, Mission mission, LocalDate assignedDate) {
        return MemberMission.builder()
                .member(Member.builder().id(memberId).build())
                .mission(mission)
                .assignedDate(assignedDate)
                .status(MissionStatus.ASSIGNED)
                .build();
    }
}
