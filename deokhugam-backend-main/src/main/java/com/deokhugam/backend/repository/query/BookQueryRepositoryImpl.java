package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.dto.book.BookDto;
import com.deokhugam.backend.dto.cursor.CursorPageResponseBookDto;
import com.deokhugam.backend.entity.Book;
import com.deokhugam.backend.entity.QBook;
import com.deokhugam.backend.mapper.BookMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BookQueryRepositoryImpl implements BookQueryRepository {

    private static final QBook b = QBook.book;
    private static final int DEFAULT_PAGE_SIZE = 20; // 기본 20개

    private final JPAQueryFactory queryFactory;
    private final BookMapper bookMapper;

    @Override
    public CursorPageResponseBookDto findByKeywordWithCursor(
            String keyword,
            String orderBy,
            Sort.Direction direction,
            UUID cursor,
            Instant after,    // 시그니처 유지(내부에선 사용 안 함)
            int limit
    ) {
        // 1) 기본 파라미터 정리
        Sort.Direction dir = (direction != null) ? direction : Sort.Direction.DESC;
        int size = (limit > 0) ? limit : DEFAULT_PAGE_SIZE;
        String sortKey = (orderBy == null || orderBy.isBlank())
                ? "title"
                : orderBy.toLowerCase(Locale.ROOT);

        // 2) 기본 where (삭제 제외 + 검색어)
        BooleanBuilder where = new BooleanBuilder();
        where.and(b.deletedAt.isNull());
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            where.and(
                    b.title.likeIgnoreCase(kw)
                            .or(b.author.likeIgnoreCase(kw))
                            .or(b.isbn.likeIgnoreCase(kw))
            );
        }

        // 3) 커서 조건(있다면): 커서 행을 DB에서 한 번 읽어서 상수로 비교
        if (cursor != null) {
            Book cursorRow = queryFactory
                    .selectFrom(b)
                    .where(b.id.eq(cursor))
                    .fetchOne();

            if (cursorRow != null) {
                applyCursorCondition(where, sortKey, dir,
                        cursor,
                        cursorRow.getTitle(),
                        cursorRow.getPublishedDate(),
                        cursorRow.getRating(),
                        cursorRow.getReviewCount(),
                        cursorRow.getCreatedAt());
            }
        }

        // 4) 정렬: 주 정렬키 + createdAt + id
        OrderSpecifier<?>[] order = buildOrder(sortKey, dir);

        // 5) 데이터 조회(+1개 더 조회해서 hasNext 판단)
        List<Book> rows = queryFactory
                .selectFrom(b)
                .where(where)
                .orderBy(order)
                .limit(size + 1L)
                .fetch();

        boolean hasNext = rows.size() > size;
        if (hasNext) {
            rows.remove(rows.size() - 1);
        }

        // 6) 전체 개수(커서 조건 제외, 검색/삭제만 반영)
        Long totalCount = queryFactory
                .select(b.count())
                .from(b)
                .where(buildBaseFilter(keyword))
                .fetchOne();
        long totalElements = (totalCount == null) ? 0L : totalCount;

        // 7) DTO 매핑
        List<BookDto> content = rows.stream().map(bookMapper::toDto).toList();

        // 8) 다음 커서 계산
        String nextCursor = null;
        Instant nextAfter = null; // 시그니처 맞춤용
        if (hasNext && !rows.isEmpty()) {
            Book last = rows.get(rows.size() - 1);
            nextCursor = last.getId().toString();
            nextAfter = last.getCreatedAt();
        }

        // 9) 응답
        return new CursorPageResponseBookDto(
                content,
                nextCursor,
                nextAfter,
                size,
                totalElements,
                hasNext
        );
    }

    /** 검색/삭제 기본 필터(커서 제외) */
    private BooleanBuilder buildBaseFilter(String keyword) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(b.deletedAt.isNull());
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            where.and(
                    b.title.likeIgnoreCase(kw)
                            .or(b.author.likeIgnoreCase(kw))
                            .or(b.isbn.likeIgnoreCase(kw))
            );
        }
        return where;
    }

    /** 정렬키 + createdAt + id 순서의 정렬 지정자 */
    private OrderSpecifier<?>[] buildOrder(String sortKey, Sort.Direction dir) {
        OrderSpecifier<?> primary;
        if ("publisheddate".equals(sortKey)) {
            primary = (dir == Sort.Direction.DESC) ? b.publishedDate.desc() : b.publishedDate.asc();
        } else if ("rating".equals(sortKey)) {
            primary = (dir == Sort.Direction.DESC) ? b.rating.desc() : b.rating.asc();
        } else if ("reviewcount".equals(sortKey)) {
            primary = (dir == Sort.Direction.DESC) ? b.reviewCount.desc() : b.reviewCount.asc();
        } else if ("createdat".equals(sortKey)) {
            primary = (dir == Sort.Direction.DESC) ? b.createdAt.desc() : b.createdAt.asc();
        } else { // title
            primary = (dir == Sort.Direction.DESC) ? b.title.desc() : b.title.asc();
        }

        OrderSpecifier<?> secondary = (dir == Sort.Direction.DESC) ? b.createdAt.desc() : b.createdAt.asc();
        OrderSpecifier<?> tieBreaker = (dir == Sort.Direction.DESC) ? b.id.desc() : b.id.asc();

        return new OrderSpecifier<?>[]{ primary, secondary, tieBreaker };
    }

    /**
     * 커서 조건을 (정렬키 → createdAt → id) 사전식으로 적용.
     * desc:
     *   key < key0 OR (key = key0 AND (createdAt < createdAt0 OR (createdAt = createdAt0 AND id < id0)))
     * asc:
     *   key > key0 OR (key = key0 AND (createdAt > createdAt0 OR (createdAt = createdAt0 AND id > id0)))
     */
    private void applyCursorCondition(BooleanBuilder where,
                                      String sortKey,
                                      Sort.Direction dir,
                                      UUID cursorId,
                                      String keyTitle0,
                                      LocalDate keyPublishedDate0,
                                      Double keyRating0,
                                      Integer keyReviewCount0,
                                      Instant createdAt0) {

        boolean desc = (dir == Sort.Direction.DESC);

        if ("publisheddate".equals(sortKey)) {
            // 날짜 비교
            if (desc) {
                where.and(
                        b.publishedDate.lt(keyPublishedDate0)
                                .or(b.publishedDate.eq(keyPublishedDate0)
                                        .and(b.createdAt.lt(createdAt0)
                                                .or(b.createdAt.eq(createdAt0).and(b.id.lt(cursorId)))))
                );
            } else {
                where.and(
                        b.publishedDate.gt(keyPublishedDate0)
                                .or(b.publishedDate.eq(keyPublishedDate0)
                                        .and(b.createdAt.gt(createdAt0)
                                                .or(b.createdAt.eq(createdAt0).and(b.id.gt(cursorId)))))
                );
            }
            return;
        }

        if ("rating".equals(sortKey)) {
            if (desc) {
                where.and(
                        b.rating.lt(keyRating0)
                                .or(b.rating.eq(keyRating0)
                                        .and(b.createdAt.lt(createdAt0)
                                                .or(b.createdAt.eq(createdAt0).and(b.id.lt(cursorId)))))
                );
            } else {
                where.and(
                        b.rating.gt(keyRating0)
                                .or(b.rating.eq(keyRating0)
                                        .and(b.createdAt.gt(createdAt0)
                                                .or(b.createdAt.eq(createdAt0).and(b.id.gt(cursorId)))))
                );
            }
            return;
        }

        if ("reviewcount".equals(sortKey)) {
            if (desc) {
                where.and(
                        b.reviewCount.lt(keyReviewCount0)
                                .or(b.reviewCount.eq(keyReviewCount0)
                                        .and(b.createdAt.lt(createdAt0)
                                                .or(b.createdAt.eq(createdAt0).and(b.id.lt(cursorId)))))
                );
            } else {
                where.and(
                        b.reviewCount.gt(keyReviewCount0)
                                .or(b.reviewCount.eq(keyReviewCount0)
                                        .and(b.createdAt.gt(createdAt0)
                                                .or(b.createdAt.eq(createdAt0).and(b.id.gt(cursorId)))))
                );
            }
            return;
        }

        if ("createdat".equals(sortKey)) {
            if (desc) {
                where.and(
                        b.createdAt.lt(createdAt0)
                                .or(b.createdAt.eq(createdAt0).and(b.id.lt(cursorId)))
                );
            } else {
                where.and(
                        b.createdAt.gt(createdAt0)
                                .or(b.createdAt.eq(createdAt0).and(b.id.gt(cursorId)))
                );
            }
            return;
        }

        // 기본: title
        if (desc) {
            where.and(
                    b.title.lt(keyTitle0)
                            .or(b.title.eq(keyTitle0)
                                    .and(b.createdAt.lt(createdAt0)
                                            .or(b.createdAt.eq(createdAt0).and(b.id.lt(cursorId)))))
            );
        } else {
            where.and(
                    b.title.gt(keyTitle0)
                            .or(b.title.eq(keyTitle0)
                                    .and(b.createdAt.gt(createdAt0)
                                            .or(b.createdAt.eq(createdAt0).and(b.id.gt(cursorId)))))
            );
        }
    }
}
