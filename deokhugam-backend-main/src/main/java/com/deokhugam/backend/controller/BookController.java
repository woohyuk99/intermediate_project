package com.deokhugam.backend.controller;

import com.deokhugam.backend.dto.book.BookCreateRequest;
import com.deokhugam.backend.dto.book.BookDto;
import com.deokhugam.backend.dto.book.BookUpdateRequest;
import com.deokhugam.backend.dto.book.NaverBookDto;
import com.deokhugam.backend.dto.cursor.CursorPageResponseBookDto;
import com.deokhugam.backend.dto.cursor.CursorPageResponsePopularBookDto;
import com.deokhugam.backend.dto.dashboard.PopularBookQuery;
import com.deokhugam.backend.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@RestController // REST 컨트롤러를 선언하는 애노테이션
@RequestMapping("/api/books") // 이 컨트롤러의 기본 URL 경로를 지정
public class BookController { // 도서 관련 HTTP 요청을 처리하는 컨트롤러 클래스 선언
    private final BookService bookService; // 서비스 의존성 필드 선언

    // 도서 등록
    @PostMapping(consumes = "multipart/form-data") // 멀티파트 폼 데이터로만 소비하는 POST 엔드포인트 선언
    public ResponseEntity<BookDto> create( // 도서 등록 요청을 처리하는 메서드 선언
                                           @RequestPart("bookData") @Valid BookCreateRequest bookData, // 'bookData' 파트를 JSON으로 역직렬화하고 검증을 수행
                                           @RequestPart(name = "thumbnailImage", required = false) MultipartFile thumbnailImage // 'thumbnailImage' 파트는 선택적 파일로 받음
    ) {
        BookDto result = bookService.create(bookData, thumbnailImage); // 서비스에 등록을 위임하고 결과 DTO를 받음
        return ResponseEntity.status(201).body(result); // 201 Created 상태 코드와 함께 결과를 응답 본문으로 반환
    }

    // 도서 상세 정보 조회
    @GetMapping("/{bookId}") // /api/books/{bookId} 요청 매핑
    public ResponseEntity<BookDto> getBookById(@PathVariable UUID bookId) { // 경로 변수로 UUID 받기
        BookDto dto = bookService.getById(bookId); // 서비스 호출
        return ResponseEntity.ok(dto); // 200 OK 응답으로 DTO 반환
    }

    // 도서 수정
    @PatchMapping(value = "/{bookId}", consumes = "multipart/form-data") // 멀티파트 PATCH 엔드포인트 선언
    public ResponseEntity<BookDto> update( // 도서 정보 수정 핸들러
                                           @PathVariable UUID bookId, // 경로 변수로 도서 ID 수신
                                           @RequestPart("bookData") @Valid BookUpdateRequest bookData, // 'bookData' 파트(JSON) 바인딩 및 검증
                                           @RequestPart(name = "thumbnailImage", required = false) MultipartFile thumbnailImage // 'thumbnailImage' 파트(선택)
    ) {
        BookDto dto = bookService.update(bookId, bookData, thumbnailImage); // 서비스에 수정 위임
        return ResponseEntity.ok(dto); // 200 OK로 결과 반환
    }

    // 도서 논리 삭제 (소프트)
    @DeleteMapping("/{bookId}") // DELETE /api/books/{bookId} 매핑
    public ResponseEntity<Void> delete(@PathVariable UUID bookId) { // 경로 변수로 도서 ID 수신
        bookService.softDelete(bookId); // 서비스에 논리 삭제 위임
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }

    // 도서 물리 삭제 (하드)
    @DeleteMapping("/{bookId}/hard") // DELETE /api/books/{bookId}/hard 요청을 매핑
    public ResponseEntity<Void> hardDelete(@PathVariable UUID bookId) { // 경로 변수로 도서 ID를 전달받는 메서드
        bookService.hardDelete(bookId); // 서비스에 물리 삭제를 위임
        return ResponseEntity.noContent().build(); // 204 No Content를 반환
    }

    // 도서 목록 조회
    @GetMapping // GET /api/books
    public ResponseEntity<CursorPageResponseBookDto> list( // 도서 목록 조회 핸들러
                                                           @RequestParam(required = false) String keyword, // 키워드 쿼리 파라미터
                                                           @RequestParam(required = false, defaultValue = "title") String orderBy, // 정렬 기준
                                                           @RequestParam(required = false, defaultValue = "DESC") Sort.Direction direction, // 정렬 방향
                                                           @RequestParam(required = false) String cursor, // 커서(UUID 문자열)
                                                           @RequestParam(required = false) Instant after, // 보조 커서(createdAt)
                                                           @RequestParam(required = false, defaultValue = "50") Integer limit // 페이지 크기
    ) {
        CursorPageResponseBookDto page = bookService.list( // 서비스 호출로 페이지 데이터 획득
                keyword, orderBy, direction, cursor, after, limit // 전달값 그대로 위임
        ); // 결과 수신
        return ResponseEntity.ok(page); // 200 OK로 페이지 응답 반환
    }

    @GetMapping("/info") // GET /api/books/info
    public ResponseEntity<NaverBookDto> getInfoByIsbn( // ISBN 조회 핸들러
                                                       @RequestParam("isbn") String isbn // 쿼리 파라미터로 ISBN 수신
    ) {
        if (isbn == null || isbn.trim().isEmpty()) { // 빈값 검증
            throw new IllegalArgumentException("isbn 파라미터는 필수입니다."); // 400 매핑 권장
        }
        NaverBookDto dto = bookService.getByIsbn(isbn); // 서비스 호출
        return ResponseEntity.ok(dto); // 200 OK 응답
    }

    @PostMapping(path = "/isbn/ocr", consumes = {"multipart/form-data"}) // 멀티파트 수신 선언
    public ResponseEntity<String> recognizeIsbn( // OCR 기반 ISBN 인식 핸들러
                                                 @RequestPart("image") MultipartFile image // form-data key=image 로 파일 수신
    ) {
        String isbn = bookService.recognizeIsbnFromImage(image); // 서비스 호출
        return ResponseEntity.ok(isbn); // 성공 시 문자열 그대로 200 OK
    }

    @GetMapping("/popular")
    public ResponseEntity<CursorPageResponsePopularBookDto> findByPopularBook(PopularBookQuery query) {
        var popularBook = bookService.findByPopularBook(query);
        return ResponseEntity.status(HttpStatus.OK).body(popularBook);
    }
}
