package com.deokhugam.backend.exception.book;

public class BookException extends RuntimeException { // 런타임 예외 확장
    private final BookErrorCode errorCode; // 에러코드 보관
    private final String overrideMessage; // 메시지 오버라이드용(선택)

    public BookException(BookErrorCode errorCode) { // 코드만 받는 생성자
        super(errorCode.getDefaultMessage()); // 기본 메시지를 부모에 전달
        this.errorCode = errorCode; // 코드 보관
        this.overrideMessage = null; // 오버라이드 메시지는 없음
    }

    public BookException(BookErrorCode errorCode, String message) { // 코드+사용자 메시지
        super(message); // 부모에 메시지 전달
        this.errorCode = errorCode; // 코드 보관
        this.overrideMessage = message; // 오버라이드 메시지 보관
    }

    public BookErrorCode getErrorCode() { // 코드 반환
        return errorCode; // 리턴
    }

    public String getEffectiveMessage() { // 최종 메시지 반환
        return (overrideMessage != null) ? overrideMessage : errorCode.getDefaultMessage(); // 오버라이드가 있으면 우선
    }
}

