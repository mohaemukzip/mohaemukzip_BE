package com.mohaemukzip.mohaemukzip_be.global.config;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Getter
public class S3Config {
    private S3Presigner presigner;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:}")
    private String secretKey;

    /**
     * 로컬: AccessKey/SecretKey 사용
     * 서버(EC2, ECS 등): IAM Role 자동 인식
     */
    @Bean
    public S3Presigner s3Presigner() {
        AwsCredentialsProvider credentialsProvider = createCredentialsProvider();

        presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        return presigner;
    }

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider = createCredentialsProvider();

        S3Client client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();

        return client;
    }

    private AwsCredentialsProvider createCredentialsProvider() {
        // AccessKey와 SecretKey가 모두 있으면 사용 (로컬 환경)
        if (hasValidCredentials()) {
           return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
            );
        }

        // 없으면 IAM Role 사용 (서버 환경)
         return DefaultCredentialsProvider.create();
    }
    private boolean hasValidCredentials() {
        return StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey);
    }

    /**
     * 스프링 종료 시 Presigner 리소스 정리
     */
    @PreDestroy
    public void cleanup() {
        if (presigner != null) {
            presigner.close();
        }
    }
}
