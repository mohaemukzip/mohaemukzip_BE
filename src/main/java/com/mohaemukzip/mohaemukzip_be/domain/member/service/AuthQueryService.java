package com.mohaemukzip.mohaemukzip_be.domain.member.service;

public interface AuthQueryService {
    boolean checkLoginIdDuplicate(String loginId);
}
