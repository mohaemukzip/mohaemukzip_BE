package com.mohaemukzip.mohaemukzip_be.domain.member.service;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCommandServiceImpl implements MemberCommandService {
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    @Transactional
    public void updateProfile(Long memberId, MemberRequestDTO.ProfileUpdateRequest req) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorStatus.MEMBER_NOT_FOUND));

        if (req.profileImageKey() != null) {
            validateProfileImageKey(req.profileImageKey());

            // 기존 이미지 삭제
            String oldKey = member.getProfileImageKey();
            if (StringUtils.hasText(oldKey)) {
                try {
                    s3Service.deleteFile(oldKey);
                } catch (Exception e) {
                    log.warn("기존 프로필 이미지 삭제 실패: {}", oldKey, e);
                }
            }

            member.updateProfileImageKey(req.profileImageKey());
        }

        // 2. 닉네임 업데이트
        if (req.nickname() != null) {
            member.updateNickname(req.nickname());
        }
    }

    private void validateProfileImageKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new BusinessException(ErrorStatus.INVALID_PROFILE_IMAGE_KEY);
        }

        if (!key.startsWith("profiles/")) {
            throw new BusinessException(ErrorStatus.INVALID_PROFILE_IMAGE_KEY);
        }

        int lastDotIndex = key.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new BusinessException(ErrorStatus.INVALID_IMAGE_EXTENSION);
        }

        String extension = key.substring(lastDotIndex + 1).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorStatus.UNSUPPORTED_IMAGE_FORMAT);
        }
    }
}
