package com.mohaemukzip.mohaemukzip_be.global.security;

import org.springframework.security.access.AccessDeniedException;

public class TermsAgreementRequiredException extends AccessDeniedException {
    public TermsAgreementRequiredException(String message) {
        super(message);
    }
}