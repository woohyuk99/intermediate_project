package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.dto.cursor.CursorPageResponseBookDto;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import java.util.UUID;

public interface BookQueryRepository { // 도서 커서 조회용 레포지토리 인터페이스 선언
    CursorPageResponseBookDto findByKeywordWithCursor( // 커서 기반 도서 목록 조회 메서드 시그니처
                                                       String keyword,                 // 제목/저자/ISBN 부분일치 검색어 (nullable)
                                                       String orderBy,                 // 정렬 기준: title|publishedDate|rating|reviewCount (nullable → 기본 title)
                                                       Sort.Direction direction,       // 정렬 방향: ASC|DESC (nullable → 기본 DESC)
                                                       UUID cursor,                    // 마지막 요소의 id (nullable)
                                                       Instant after,                  // 마지막 요소의 createdAt (nullable)
                                                       int limit                       // 페이지 크기(기본 50)
    );
}
