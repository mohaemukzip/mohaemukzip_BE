package com.mohaemukzip.mohaemukzip_be.domain.member.controller;

import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.dto.MemberResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.member.service.command.member.MemberCommandService;
import com.mohaemukzip.mohaemukzip_be.domain.member.service.query.member.MemberQueryService;
import com.mohaemukzip.mohaemukzip_be.domain.recipe.dto.RecipeResponseDTO;
import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import com.mohaemukzip.mohaemukzip_be.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "Member", description = "회원(멤버) 관련 API")
@Validated
public class MemberController {
    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    @Operation(summary = "마이페이지 조회", description = "유저 프로필 + 레벨 정보")
    @GetMapping("/me/mypage")
    public ApiResponse<MemberResponseDTO.MyPageDTO> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        MemberResponseDTO.MyPageDTO myPage = memberQueryService.getMyPage(memberId);

        return ApiResponse.onSuccess(myPage);
    }

    @Operation(summary = "프로필 수정", description = "이미지/닉네임 중 하나이상 업데이트")
    @PatchMapping("/me/profile")
    public ApiResponse<Void> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody MemberRequestDTO.ProfileUpdateRequest req) {

        Long memberId = userDetails.getMember().getId();

        memberCommandService.updateProfile(memberId, req);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/me/recently-viewed")
    @Operation(summary = "최근 본 레시피 조회 API", description = "사용자가 최근에 조회한 레시피 목록을 반환합니다.")
    public ApiResponse<List<RecipeResponseDTO.RecipePreviewDTO>> getRecentlyViewedRecipes(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<RecipeResponseDTO.RecipePreviewDTO> recipes = memberQueryService.getRecentlyViewedRecipes(
                userDetails.getMember().getId()
        );

        return ApiResponse.onSuccess(recipes);
    }

    @GetMapping("/me/bookmarks")
    @Operation(
            summary = "저장된 레시피 목록 조회 API",
            description = "사용자가 북마크한 레시피 목록 (페이지당 10개, 최신순)"
    )
    public ApiResponse<RecipeResponseDTO.RecipePreviewListDTO> getBookmarkedRecipes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RecipeResponseDTO.RecipePreviewListDTO bookmarkedRecipes = memberQueryService.getBookmarkedRecipes(
                userDetails.getMember().getId(),
                page
        );

        return ApiResponse.onSuccess(bookmarkedRecipes);
    }
}
