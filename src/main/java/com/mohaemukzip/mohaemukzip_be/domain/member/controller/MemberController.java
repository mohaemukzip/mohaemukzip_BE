package com.mohaemukzip.mohaemukzip_be.domain.member.controller;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.service.MemberCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.member.service.MemberQueryService;
import com.mohaemukzip.mohaemukzip_be.global.exception.BusinessException;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.response.code.status.ErrorStatus;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "Member", description = "회원(멤버) 관련 API")
@Validated
public class MemberController {
    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    @Operation(summary = "마이페이지 프로필 조회", description = "유저 프로필 + 레벨 정보")
    @GetMapping("/profile")
    public ApiResponse<MemberResponseDTO.GetMemberDTO> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new BusinessException(ErrorStatus.TOKEN_MISSING);
        }

        Long memberId = userDetails.getMember().getId();
        MemberResponseDTO.GetMemberDTO profile = memberQueryService.getMyProfile(memberId);

        return ApiResponse.onSuccess(profile);
    }

    @Operation(summary = "프로필 수정", description = "이미지/닉네임 중 하나이상 업데이트")
    @PatchMapping("/profile")
    public ApiResponse<Void> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MemberRequestDTO.ProfileUpdateRequest req) {

        Long memberId = userDetails.getMember().getId();

        memberCommandService.updateProfile(memberId, req);
        return ApiResponse.onSuccess(null);
    }
}
