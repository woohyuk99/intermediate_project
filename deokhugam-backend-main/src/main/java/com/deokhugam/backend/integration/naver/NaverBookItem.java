package com.deokhugam.backend.integration.naver; // 네이버 연동 패키지

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 불필요 필드 무시 애노테이션 임포트

@JsonIgnoreProperties(ignoreUnknown = true) // 정의 안 한 응답 필드는 무시
public class NaverBookItem { // 개별 도서 항목 DTO 시작
    public String title; // 제목(HTML 태그 포함 가능)
    public String author; // 저자 문자열(복수 저자 문자열이 들어올 수 있음)
    public String description; // 설명(HTML 태그 포함 가능)
    public String publisher; // 출판사
    public String pubdate; // 출간일(yyyyMMdd)
    public String image; // 썸네일 절대 URL(비어있을 수 있음)
} // 클래스 끝
