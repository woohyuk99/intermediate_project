package com.deokhugam.backend.integration.ocr; // OCR 연동 패키지

import org.springframework.web.multipart.MultipartFile; // 업로드 파일 타입 임포트

public interface OcrClient { // OCR 호출용 포트 인터페이스 선언
    String recognizeText(MultipartFile image); // 이미지를 OCR 해 원문 텍스트를 반환하는 메서드
}
