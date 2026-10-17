package com.mohaemukzip.mohaemukzip_be.domain.member.event;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.enums.LoginType;

public record MemberSignedUpEvent(Long memberId, String nickname, LoginType loginType) {
}
