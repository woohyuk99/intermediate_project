package com.deokhugam.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.File;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class FileConfigDev implements FileConfig {

    @Value("${app.file.upload-dir}")
    private String uploadDir;


    @Override
    public String getUploadDir() {
        return uploadDir;
    }

    @Override
    public File getAttachFileUploadDirFile() {
        File dir = new File(uploadDir, "attachments");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("첨부파일 업로드 디렉토리 생성 실패: " + dir.getAbsolutePath());
        }
        return dir;
    }
}
