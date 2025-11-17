package com.deokhugam.backend.integration.naver; // 네이버 연동 패키지

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 불필요 필드 무시를 위한 애노테이션 임포트

@JsonIgnoreProperties(ignoreUnknown = true) // 응답에 정의되지 않은 필드는 무시
public class NaverSearchResponse { // 네이버 도서 검색 루트 응답 DTO 시작
    public NaverBookItem[] items; // 결과 항목 배열(우리가 실제로 사용하는 필드)
} // 클래스 끝
