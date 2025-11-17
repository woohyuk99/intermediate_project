package com.deokhugam.backend.mapper;

import com.deokhugam.backend.dto.book.BookCreateRequest;
import com.deokhugam.backend.dto.book.BookDto;
import com.deokhugam.backend.entity.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN) // 스프링 빈으로 등록 + 매핑 누락시 경고 표시
public interface BookMapper { // BookMapper 인터페이스 선언

    // 요청 DTO → 엔티티
    @Mapping(target = "id", ignore = true) // 기본키는 자동 생성되므로 매핑 제외
    @Mapping(target = "createdAt", ignore = true) // 생성 시각 자동 처리
    @Mapping(target = "updatedAt", ignore = true) // 수정 시각 자동 처리
    @Mapping(target = "deletedAt", ignore = true) // 논리삭제 컬럼 무시
    @Mapping(target = "reviewCount", constant = "0") // 리뷰 수 기본값 0 설정
    @Mapping(target = "rating", constant = "0.0") // 평점 기본값 0.0 설정
//    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl") // 썸네일 URL은 서비스 계층에서 주입
    // 🔒 타입변환 경로 차단: 파라미터 thumbnailUrl 그대로 대입
    @Mapping(target = "thumbnailUrl", expression = "java(thumbnailUrl)")
    @Mapping(target = "isbn", expression = "java(normalizeIsbn(request.isbn()))") // ISBN 정규화 처리
    Book toEntity(BookCreateRequest request, String thumbnailUrl); // 생성 요청 → 엔티티 매핑 메서드

    // 엔티티 → 응답 DTO
    // toDto도 동일하게 명시 (String→String 변환 우회)
    @Mapping(target = "thumbnailUrl", expression = "java(book.getThumbnailUrl())")
    BookDto toDto(Book book); // MapStruct가 동일 필드명을 자동 매핑

    // --- 아래는 커스텀 헬퍼 메서드 ---
    default String normalizeIsbn(String raw) { // ISBN 정규화용 헬퍼 메서드
        if (raw == null) return null; // null이면 그대로 null 반환
        String digits = raw.replaceAll("-", "").trim(); // 하이픈 제거 후 공백 제거
        return digits.isEmpty() ? null : digits; // 비어있으면 null, 아니면 정규화된 문자열 반환
    }
}
