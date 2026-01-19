package com.mohaemukzip.mohaemukzip_be.global.s3;

import com.mohaemukzip.mohaemukzip_be.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/s3")
@Tag(name = "Image", description = "S3 이미지 업로드 관련 API")
public class S3Controller {

    private final S3Service s3Service;

    /**
     *  업로드용 Presigned URL 발급
     * 프론트에서 파일 업로드 전에 요청
     */
    @Operation(summary = "프로필 이미지 업로드 URL 발급")
    @PostMapping("/profile/upload-url")
    public ApiResponse<S3ResponseDTO.PresignedUrlResDTO> getPresignedUploadUrl(
            @Valid @RequestBody S3RequestDTO.ProfileImageReqDTO req
    ) {
        S3ResponseDTO.PresignedUrlResDTO response = s3Service.generateProfileUploadUrl(req);
        return ApiResponse.onSuccess(response);
    }
}
