package com.deokhugam.backend.service;

import com.deokhugam.backend.dto.book.BookCreateRequest;
import com.deokhugam.backend.dto.book.BookDto;
import com.deokhugam.backend.dto.book.BookUpdateRequest;
import com.deokhugam.backend.dto.book.NaverBookDto;
import com.deokhugam.backend.dto.cursor.CursorPageResponseBookDto;
import com.deokhugam.backend.dto.cursor.CursorPageResponsePopularBookDto;
import com.deokhugam.backend.dto.dashboard.PopularBookQuery;
import com.deokhugam.backend.entity.Book;
import com.deokhugam.backend.exception.book.BookErrorCode;
import com.deokhugam.backend.exception.book.BookException;
import com.deokhugam.backend.integration.naver.NaverBookClient;
import com.deokhugam.backend.integration.ocr.OcrClient;
import com.deokhugam.backend.mapper.BookMapper;
import com.deokhugam.backend.repository.BookRepository;
import com.deokhugam.backend.repository.ReviewRepository;
import com.deokhugam.backend.repository.query.BookQueryRepository;
import com.deokhugam.backend.repository.query.PopularBookQueryRepository;
import com.deokhugam.backend.storage.FileStorage;
import com.deokhugam.backend.support.IsbnUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final FileStorage fileStorage;
    private final BookMapper bookMapper;
    private final BookQueryRepository bookQueryRepository;
    private final PopularBookQueryRepository popularBookQueryRepository;
    private final NaverBookClient naverBookClient; // 네이버 클라이언트 의존성
    private final OcrClient ocrClient; // OCR 호출 포트 의존성 보관

    // ISBN-13: 13자리 숫자(하이픈 옵션), ISBN-10도 보조적으로 지원
    private static final Pattern ISBN13 = Pattern.compile("\\b97[89][-\\s]?\\d{1,5}[-\\s]?\\d{1,7}[-\\s]?\\d{1,7}[-\\s]?\\d\\b"); // ISBN-13 정규식
    private static final Pattern ISBN10 = Pattern.compile("\\b\\d{1,5}[-\\s]?\\d{1,7}[-\\s]?\\d{1,7}[-\\s]?[\\dXx]\\b"); // ISBN-10 정규식


    // 도서 등록
    @Transactional // 데이터 정합성을 보장하기 위한 트랜잭션 경계 선언
    public BookDto create(BookCreateRequest request, MultipartFile thumbnailImage) { // 도서 등록을 수행하는 서비스 메서드 선언
        String normalizedIsbn = IsbnUtils.normalizeOrNull(request.isbn()); // ISBN 값을 정규화하여 변수에 보관
        if (normalizedIsbn != null && bookRepository.existsByIsbn(normalizedIsbn)) { // 정규화된 ISBN이 존재하고 이미 저장소에 있다면
            throw new IllegalStateException("이미 존재하는 ISBN입니다: " + normalizedIsbn); // 중복 ISBN 예외를 던짐(글로벌 Advice에서 409로 매핑 권장)
        }

        String thumbnailUrl = null; // 업로드된 썸네일 URL을 보관할 변수를 null로 초기화
        System.out.println("[CREATE] thumbnail null? " + (thumbnailImage == null) + ", empty? " + (thumbnailImage != null && thumbnailImage.isEmpty()));

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) { // 파일이 전달되었고 내용이 비어있지 않다면
            String savedName = fileStorage.saveAttachFile(thumbnailImage); // 저장된 파일명
            System.out.println("[CREATE] savedName=" + savedName);
            thumbnailUrl = fileStorage.getAttachFileUrl(savedName);        // 접근 가능한 URL 생성
            System.out.println("[CREATE] thumbnailUrl=" + thumbnailUrl);
        }

        Book entity = bookMapper.toEntity(request, thumbnailUrl); // 요청 DTO와 썸네일 URL을 이용해 엔티티를 생성
        System.out.println("[CREATE] entity.thumbnailUrl=" + entity.getThumbnailUrl());

        if (thumbnailUrl != null && entity.getThumbnailUrl() != null
                && !entity.getThumbnailUrl().equals(thumbnailUrl)) {
            entity.updateThumbnailUrl(thumbnailUrl); // URL 원본 강제 복구
            System.out.println("[CREATE] force-set original thumbnailUrl");
        }

        Book saved = bookRepository.save(entity);
        System.out.println("[CREATE] saved.thumbnailUrl=" + saved.getThumbnailUrl());
        // MapStruct 대신 직접 DTO 생성 (thumbnailUrl 원본 그대로 박제)
        BookDto dto = new BookDto(
                saved.getId(),
                saved.getTitle(),
                saved.getAuthor(),
                saved.getDescription(),
                saved.getPublisher(),
                saved.getPublishedDate(),
                saved.getIsbn(),
                saved.getThumbnailUrl(),   // ✅ 엔티티의 ‘정상’ 값 그대로
                saved.getReviewCount(),
                saved.getRating(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
        System.out.println("[CREATE] dto.thumbnailUrl=" + dto.thumbnailUrl());
        return dto;

    }

    // 도서 상세 정보 조회
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public BookDto getById(UUID bookId) { // 도서 상세 조회 비즈니스 메서드
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId) // 논리삭제 안 된 도서만 조회
                .orElseThrow(() -> new IllegalStateException("존재하지 않거나 삭제된 도서입니다.")); // 없으면 예외 발생
        return bookMapper.toDto(book); // 엔티티를 DTO로 변환해 반환
    }

    // 인기 도서 목록 조회
    @Transactional(readOnly = true)
    public CursorPageResponsePopularBookDto findByPopularBook(PopularBookQuery query) {
        return popularBookQueryRepository.findByPopularBookIdWithCursor(query);
    }

    // 도서 수정
    @Transactional // 쓰기 트랜잭션
    public BookDto update(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage) { // 도서 정보 수정 메서드
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId) // 유효한 도서 엔티티 조회
                .orElseThrow(() -> new IllegalStateException("존재하지 않거나 삭제된 도서입니다.")); // 없으면 예외

        book.update(request); // 제목/저자/소개/출판사/출간일만 변경(엔티티 도메인 로직 사용)

        // 파일이 오면 교체
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            // (선택) 기존 로컬 파일 정리 – 기존 URL이 /attachments/ 형태면 파일명 뽑아 삭제
            String oldUrl = book.getThumbnailUrl();
            if (oldUrl != null && oldUrl.contains("/attachments/")) {
                String oldName = oldUrl.substring(oldUrl.lastIndexOf('/') + 1);
                fileStorage.deleteAttachmentBySavedName(oldName);
            }

            String savedName = fileStorage.saveAttachFile(thumbnailImage);
            String newUrl = fileStorage.getAttachFileUrl(savedName);

            // 가드
            book.updateThumbnailUrl(newUrl); // 먼저 세팅

            // 혹시 세팅 과정에서 변조됐는지 방어 (가드)
            if (newUrl != null && !newUrl.equals(book.getThumbnailUrl())) {
                book.updateThumbnailUrl(newUrl);
            }

            // 엔티티에 최종 URL 반영 (세터나 도메인 메서드가 있으면 그걸 사용)
            try {
                var f = Book.class.getDeclaredField("thumbnailUrl");
                f.setAccessible(true);
                f.set(book, newUrl);
            } catch (Exception e) {
                throw new IllegalStateException("썸네일 URL 갱신 실패", e);
            }
        }

        // JPA 더티체킹으로 변경사항 자동 플러시 → 별도 save 호출 불필요
        return bookMapper.toDto(book); // 변경된 엔티티를 DTO로 변환해 반환
    }

    @Transactional // 쓰기 트랜잭션 시작
    public void softDelete(UUID bookId) { // 도서 논리 삭제 메서드
        Book book = bookRepository.findByIdAndDeletedAtIsNull(bookId) // 논리삭제되지 않은 도서만 조회
                .orElseThrow(() -> new IllegalStateException("존재하지 않거나 이미 삭제된 도서입니다.")); // 없으면 예외
        book.softDelete(); // 엔티티의 논리 삭제 도메인 메서드 호출(deletedAt=now)
        // JPA 더티체킹으로 자동 반영되므로 save 호출 불필요
    }

    // 도서 물리 삭제 (하드)
    @Transactional // 쓰기 트랜잭션 경계를 지정
    public void hardDelete(UUID bookId) { // 물리 삭제를 수행하는 메서드 선언
        Book book = bookRepository.findById(bookId) // ID로 도서를 조회(논리삭제 포함)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 도서입니다.")); // 없으면 예외 발생


        try { // FK 제약 위반 등 삭제 실패 상황을 포착하기 위한 블록 시작
            bookRepository.delete(book); // JPA를 통해 엔티티를 물리 삭제 요청
            bookRepository.flush(); // 즉시 플러시하여 DB 레벨 제약 위반을 여기서 감지

            // (선택) dev 로컬 파일 정리
            String url = book.getThumbnailUrl();
            if (url != null && url.contains("/attachments/")) {
                String name = url.substring(url.lastIndexOf('/') + 1);
                fileStorage.deleteAttachmentBySavedName(name);
            }
        } catch (DataIntegrityViolationException e) { // 외래키 제약 등으로 삭제가 거부된 경우
            throw new IllegalStateException("연관 데이터(리뷰/댓글/좋아요 등)가 있어 물리 삭제할 수 없습니다.", e); // 의미있는 메시지로 감싸서 재던짐
        }
    }

    public CursorPageResponseBookDto list( // 도서 목록 조회 비즈니스 메서드
                                           String keyword, // 검색어
                                           String orderBy, // 정렬 기준
                                           Sort.Direction direction, // 정렬 방향
                                           String cursor, // 커서 문자열(UUID)
                                           Instant after, // 보조 커서
                                           Integer limit // 페이지 크기
    ) {
        UUID cursorId = (cursor == null || cursor.isBlank()) ? null : UUID.fromString(cursor); // 커서 문자열을 UUID로 변환
        int size = (limit == null) ? 50 : limit; // 기본 페이지 크기를 50으로 설정
        return bookQueryRepository.findByKeywordWithCursor( // 레포지토리에 위임
                keyword, // 검색어 전달
                orderBy, // 정렬 키 전달
                direction, // 정렬 방향 전달
                cursorId, // 변환된 커서 UUID 전달
                after, // 보조 커서 전달
                size // 페이지 크기 전달
        );
    }

    // ISBN으로 도서 조회 API
    public NaverBookDto getByIsbn(String isbn) { // ISBN으로 도서 조회
        return naverBookClient.findByIsbn(isbn); // 네이버 클라이언트에 위임
    }

    // OCR 기반 ISBN 인식 API
    public String recognizeIsbnFromImage(MultipartFile image) { // OCR→ISBN 추출 서비스 메서드
        if (image == null || image.isEmpty()) { // 파일 검증
            throw new BookException(BookErrorCode.OCR_RECOGNITION_FAILED); // 빈 파일도 동일 에러로 처리(프론트 일관성)
        }
        String text = ocrClient.recognizeText(image); // OCR 호출로 전체 텍스트 획득

        String isbn = findFirst(text, ISBN13); // ISBN-13 우선 탐색
        if (isbn == null) { // 없으면
            isbn = findFirst(text, ISBN10); // ISBN-10 보조 탐색
        }
        if (isbn == null) { // 둘 다 없으면
            throw new BookException(BookErrorCode.OCR_RECOGNITION_FAILED); // 스펙대로 400 응답 유도
        }

        String normalized = IsbnUtils.normalizeOrNull(isbn); // 하이픈/공백 제거 등 정규화
        if (normalized == null) { // 정규화 실패 시
            throw new BookException(BookErrorCode.OCR_RECOGNITION_FAILED); // 동일 코드로 처리
        }
        return normalized; // 성공 시 정규화된 ISBN 반환
    }

    private String findFirst(String text, Pattern pattern) { // 정규식 첫 매칭 반환 헬퍼
        if (text == null || text.isBlank()) return null; // 텍스트 없으면 null
        Matcher m = pattern.matcher(text); // 매처 생성
        return m.find() ? m.group() : null; // 매칭 성공 시 해당 문자열, 아니면 null
    }

    /** 단일 도서의 평균/개수를 재계산하여 Book 테이블에 반영 */
    @Transactional
    public void refreshForBook(UUID bookId) {
        long count = reviewRepository.countByBookId(bookId);
        Double avgOrNull = reviewRepository.avgRatingByBookId(bookId);
        double avg = (avgOrNull == null) ? 0.0 : avgOrNull;
        // 보기 좋게 1자리 반올림(원하면 제거)
        double rounded = Math.round(avg * 10.0) / 10.0;

        bookRepository.updateStats(bookId, rounded, (int) count);
    }


}
