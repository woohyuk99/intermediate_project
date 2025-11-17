package com.deokhugam.backend.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("prod")
class S3ConnectionTest {

    // ✅ Spring이 자동으로 S3Client를 Bean으로 주입합니다.
    @Autowired
    private S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Test
    @DisplayName("S3 연결 테스트 - 간단한 텍스트 업로드")
    void s3ConnectionCheck() {
        try {
            String content = "S3 connection test file!";
            String key = "test/" + UUID.randomUUID() + "_s3_test.txt";

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("text/plain")
                    .build();


            s3Client.putObject(request, RequestBody.fromString(content, StandardCharsets.UTF_8));

            String uploadedUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;
            System.out.println("✅ S3 연결 성공! 업로드된 파일 URL:");
            System.out.println(uploadedUrl);

        } catch (Exception e) {
            System.out.println("❌ S3 연결 실패: " + e.getMessage());
            throw e;
        }
    }
}
