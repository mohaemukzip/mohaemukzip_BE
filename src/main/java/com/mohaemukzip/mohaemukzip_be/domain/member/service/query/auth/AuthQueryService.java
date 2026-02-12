package com.mohaemukzip.mohaemukzip_be.domain.member.service.query.auth;

public interface AuthQueryService {
    boolean checkLoginIdDuplicate(String loginId);
}
