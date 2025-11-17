package com.deokhugam.backend.integration.naver; // 네이버 연동 패키지 선언

import com.deokhugam.backend.dto.book.NaverBookDto; // 응답 DTO 임포트

public interface NaverBookClient { // 네이버 도서 검색 클라이언트 인터페이스
    NaverBookDto findByIsbn(String rawIsbn); // ISBN으로 조회해 NaverBookDto 반환하는 메서드 시그니처
}
