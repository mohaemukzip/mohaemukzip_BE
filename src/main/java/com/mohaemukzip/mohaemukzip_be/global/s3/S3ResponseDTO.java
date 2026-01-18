package com.mohaemukzip.mohaemukzip_be.global.s3;

public class S3ResponseDTO {
    public record PresignedUrlResDTO(
            String key,
            String presignedUrl
    ) {}
}
