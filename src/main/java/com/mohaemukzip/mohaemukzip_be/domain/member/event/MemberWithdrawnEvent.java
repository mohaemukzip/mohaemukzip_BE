package com.mohaemukzip.mohaemukzip_be.domain.member.event;

import com.mohaemukzip.mohaemukzip_be.domain.member.entity.enums.LoginType;

public record MemberWithdrawnEvent(Long memberId, String nickname, LoginType loginType) {
}
