package com.mohaemukzip.mohaemukzip_be.global.s3;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class S3RequestDTO {

    public record PresignedUploadReqDTO (
        @NotBlank(message = "폴더명은 필수입니다.")
        String folder,

        @NotEmpty(message = "파일명이 필요합니다.")
        List<String> fileNames,

        @NotEmpty(message = "Content-Type이 필요합니다.")
        List<String> contentTypes
    ) {}

    public record ProfileImageReqDTO(
            @Schema(description = "파일명", example = "profileImage.png")
            @NotBlank(message = "파일명이 필요합니다.") String fileName,
            @Schema(description = "Content-Type", example = "image/png")
            @NotBlank(message = "Content-Type이 필요합니다.") String contentType
    ) {}
}