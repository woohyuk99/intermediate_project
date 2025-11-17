package com.deokhugam.backend.storage.impl;

import com.deokhugam.backend.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;


@Component
@RequiredArgsConstructor
@Slf4j
@Profile("prod")
public class FileStorageS3 implements FileStorage {

    private final S3Client s3Client;

    // === YML 속성 ===
    // cloud.aws.s3.bucket: 버킷명 (필수)
    // cloud.aws.s3.prefix: 업로드 prefix(옵션, 기본 uploads)
    // cloud.aws.region.static: 리전 (필수)

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.prefix:uploads}")
    private String prefix;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Override
    public String saveAttachFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다");
        }

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains(".")) ?
                originalName.substring(originalName.lastIndexOf('.')) : "";

        // ✅ dev와 동일 포맷(하이픈 없음) 유지
        String renamed = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                .format(LocalDateTime.now()) + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        String key = prefix + "/attachments/" + renamed;

        try {
            byte[] bytes = file.getBytes(); // 썸네일이라 안전
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(Optional.ofNullable(file.getContentType()).orElse("application/octet-stream"))
                    .cacheControl("public, max-age=31536000, immutable")
                    .build();
            s3Client.putObject(putReq, RequestBody.fromBytes(bytes));
            log.info("S3 업로드 완료: s3://{}/{}", bucketName, key);
        } catch (Exception e) {
            log.error("S3 업로드 실패 key={}", key, e);
            throw new RuntimeException("파일 업로드 실패: " + e.getMessage(), e);
        }

        return renamed;
    }

    @Override
    public void deleteAttachmentBySavedName(String savedFileName) {
        if (savedFileName == null || savedFileName.isBlank()) return;
        String key = prefix + "/attachments/" + savedFileName;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
            log.info("S3 삭제 완료: s3://{}/{}", bucketName, key);
        } catch (Exception e) {
            log.warn("S3 삭제 실패(무시 가능): s3://{}/{}", bucketName, key, e);
        }
    }

    @Override
    public String getAttachFileUrl(String filename) {
        String key = prefix + "/attachments/" + filename;
        String url = s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build())
                .toExternalForm();
        log.info("[S3-URL] bucket={}, region={}, key={}, url={}", bucketName, region, key, url);
        return url;
    }
}
