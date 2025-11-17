package com.deokhugam.backend.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;

import com.deokhugam.backend.config.FileConfig;

@RestController
@RequiredArgsConstructor
@Profile("dev")
@RequestMapping("/api/attachments")   // 클래스 레벨 고정
public class FileController {

    private final FileConfig fileConfig;

    @GetMapping("/{filename:.+}")     // 파일명에 점(.) 포함 허용
    public ResponseEntity<Resource> getAttachmentByName(
            @PathVariable String filename,
            @RequestParam(name="download", defaultValue="false") boolean download
    ) throws MalformedURLException {

        File base = fileConfig.getAttachFileUploadDirFile();
        System.out.println("[DEV-READ] baseDir=" + base.getAbsolutePath());

        Path path = new File(base, filename).toPath();
        System.out.println("[DEV-READ] try=" + path.toAbsolutePath() + " exists=" + Files.exists(path));

        if (!Files.exists(path)) return ResponseEntity.notFound().build();

        UrlResource resource = new UrlResource(path.toUri());
        MediaType mediaType = MediaTypeFactory.getMediaType(filename)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }

    // 임시 핑 엔드포인트: 매핑 확인용
    @GetMapping("/_ping")
    public String ping() { return "ok"; }
}
