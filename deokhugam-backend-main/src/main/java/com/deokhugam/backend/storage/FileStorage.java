package com.deokhugam.backend.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    /** 첨부파일 저장(이미지 포함). 저장소에 저장 후 '저장된 파일명(리네임)'을 반환 */
    String saveAttachFile(MultipartFile file);

    /** 저장된 첨부파일(파일명 기준) 삭제 */
    void deleteAttachmentBySavedName(String savedFileName);

    /** 저장된 파일명(리네임) → 접근 가능한 URL (dev: /attachments/{name}, prod: S3 정적 URL) */
    String getAttachFileUrl(String savedFileName);
}
