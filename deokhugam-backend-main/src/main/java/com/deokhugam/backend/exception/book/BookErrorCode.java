package com.deokhugam.backend.exception.book;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum BookErrorCode { // 도서 도메인 전용 에러코드 enum 선언
    OCR_RECOGNITION_FAILED(HttpStatus.BAD_REQUEST, "OCR_RECOGNITION_FAILED", "이미지에서 ISBN 인식에 실패했습니다."); // 요구 스펙에 맞춘 코드/메시지

    private final HttpStatus httpStatus; // HTTP 상태 보관
    private final String code; // 응답 JSON의 code 값
    private final String defaultMessage; // 기본 메시지

    public HttpStatus getHttpStatus() { // 상태 반환
        return httpStatus; // 리턴
    }

    public String getCode() { // 코드 반환
        return code; // 리턴
    }

    public String getDefaultMessage() { // 기본 메시지 반환
        return defaultMessage; // 리턴
    }
}
