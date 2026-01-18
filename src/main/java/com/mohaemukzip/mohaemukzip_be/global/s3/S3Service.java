package com.mohaemukzip.mohaemukzip_be.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private static final Duration UPLOAD_URL_DURATION = Duration.ofMinutes(5);
    private static final Duration VIEW_URL_DURATION = Duration.ofMinutes(10);

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    //  S3에 이미지 업로드용 Presigned URL 발급 (PUT 방식)
    public S3ResponseDTO.PresignedUrlResDTO generateProfileUploadUrl(S3RequestDTO.ProfileImageReqDTO req) {
        String extension = extractFileExtension(req.fileName());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식: " + extension);
        }

        String key = "profiles/" + UUID.randomUUID() + "." +  extension;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(req.contentType())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                r -> r.signatureDuration(UPLOAD_URL_DURATION).putObjectRequest(putRequest)
        );

        return new S3ResponseDTO.PresignedUrlResDTO(key, presignedRequest.url().toString());
    }
    // 2. 이미지 조회 URL
    public String generateViewPresignedUrl(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(VIEW_URL_DURATION)
                .getObjectRequest(getRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    // 3. 이미지 삭제
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        } catch (Exception e) { }
    }

    // 4. S3 URL에서 Key 추출
    public String extractKeyFromUrl(String url) {
        if (!StringUtils.hasText(url)) return null;

        if (!url.contains("amazonaws.com")) { return url; }
        try {
            int idx = url.indexOf(".amazonaws.com/");
            if (idx == -1) {
                throw new IllegalArgumentException("잘못된 S3 URL 형식입니다");
            }

            String afterDomain = url.substring(idx + ".amazonaws.com/".length());

            // bucket 이름이 있으면 제거
            if (afterDomain.startsWith(bucket + "/")) {
                afterDomain = afterDomain.substring(bucket.length() + 1);
            }

            // 쿼리 파라미터 제거 (presigned URL 파라미터)
            int queryIdx = afterDomain.indexOf('?');
            if (queryIdx != -1) {
                afterDomain = afterDomain.substring(0, queryIdx);
            }

            return afterDomain;
        } catch (Exception e) {
            throw new IllegalArgumentException("URL에서 key 추출 실패: " + url, e);
        }
    }

    // 5. 프로필 이미지 URL 생성
    public String generateProfileImageUrl(String s3KeyOrUrl) {
        // S3 Key가 없으면 기본 이미지
        if (!StringUtils.hasText(s3KeyOrUrl)) {
            return null;
        }

        String key = s3KeyOrUrl.contains("amazonaws.com")
                ? extractKeyFromUrl(s3KeyOrUrl)
                : s3KeyOrUrl;

        return generateViewPresignedUrl(key);
    }

    /**
     * 파일 확장자 추출
     */
    private String extractFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("파일 확장자가 없습니다: " + fileName);
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

}