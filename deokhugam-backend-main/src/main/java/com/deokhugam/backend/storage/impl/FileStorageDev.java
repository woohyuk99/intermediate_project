// src/main/java/com/deokhugam/backend/storage/impl/FileStorageDev.java
package com.deokhugam.backend.storage.impl;

import com.deokhugam.backend.config.FileConfig;
import com.deokhugam.backend.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class FileStorageDev implements FileStorage {

    private final FileConfig fileConfig;

    @Value("${app.web.public-base-path:}")   // 없으면 빈 문자열
    private String publicBasePath;

    @Override
    public String saveAttachFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 저장할 수 없습니다");
        }
        File dir = fileConfig.getAttachFileUploadDirFile();
        System.out.println("[DEV-SAVE] baseDir=" + dir.getAbsolutePath());
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains(".")) ?
                originalName.substring(originalName.lastIndexOf('.')) : "";
        String renamed = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss") // <-- 하이픈 제거
                .format(LocalDateTime.now()) + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        File dest = new File(dir, renamed);

        try {
            System.out.println("[DEV-SAVE] dest=" + dest.getAbsolutePath());
            file.transferTo(dest);
            System.out.println("[DEV-SAVE] exists_after=" + dest.exists());
            System.out.println("[DEV-SAVE] returned_name=" + renamed);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + dest.getAbsolutePath(), e);
        }
        return renamed;
    }


    @Override
    public void deleteAttachmentBySavedName(String savedFileName) {
        if (savedFileName == null || savedFileName.isBlank()) return;
        File dir = fileConfig.getAttachFileUploadDirFile();
        File target = new File(dir, savedFileName);
        if (target.exists() && !target.delete()) {
            throw new RuntimeException("첨부 파일 삭제 실패: " + target.getAbsolutePath());
        }
    }

    @Override
    public String getAttachFileUrl(String filename) {
        // publicBasePath가 "/sb/deokhugam" 같은 베이스라면 붙여주고,
//        String base = (publicBasePath == null) ? "" : publicBasePath.trim();
//        if (!base.isEmpty() && !base.startsWith("/")) base = "/" + base;
//        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);

        // 여기 핵심: /api/attachments 로 고정
//        return base + "/api/attachments/" + filename;
        return "/api/attachments/" + filename; // <-- 고정
    }
}
