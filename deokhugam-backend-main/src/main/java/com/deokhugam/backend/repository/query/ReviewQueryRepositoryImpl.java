package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.entity.QBook;
import com.deokhugam.backend.entity.QReview;
import com.deokhugam.backend.entity.QUser;
import com.deokhugam.backend.entity.Review;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepositoryImpl implements ReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    QReview r = QReview.review;
    QUser u = QUser.user;
    QBook b = QBook.book;

    @Override
    public List<Review> findWithCreatedAtSort(UUID bookId, Instant cursor, boolean isAsc, String keyword, Pageable pageable) {
        BooleanBuilder condition = baseCondition(bookId, keyword);

        if (cursor != null) {
            if (isAsc) condition.and(r.createdAt.gt(cursor));
            else condition.and(r.createdAt.lt(cursor));
        }

        OrderSpecifier<?> order = isAsc ? r.createdAt.asc() : r.createdAt.desc();

        return queryFactory
                .selectFrom(r)
                .leftJoin(r.user, u).fetchJoin()
                .leftJoin(r.book, b).fetchJoin()
                .where(condition)
                .orderBy(order)
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Review> findWithRatingSort(UUID bookId, Integer ratingCursor, Instant createdAtCursor, boolean isAsc, String keyword, Pageable pageable) {
        BooleanBuilder condition = baseCondition(bookId, keyword);

        // 커서 조건
        if (ratingCursor != null && createdAtCursor != null) {
            if (isAsc) {
                condition.and(
                        r.rating.gt(ratingCursor)
                                .or(r.rating.eq(ratingCursor).and(r.createdAt.lt(createdAtCursor)))
                );
            } else {
                condition.and(
                        r.rating.lt(ratingCursor)
                                .or(r.rating.eq(ratingCursor).and(r.createdAt.lt(createdAtCursor)))
                );
            }
        }

        // 정렬 기준
        OrderSpecifier<?> order1 = isAsc ? r.rating.asc() : r.rating.desc();
        OrderSpecifier<?> order2 = r.createdAt.desc();

        return queryFactory
                .selectFrom(r)
                .leftJoin(r.user, u).fetchJoin()
                .leftJoin(r.book, b).fetchJoin()
                .where(condition)
                .orderBy(order1, order2)
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public long countByBookIdAndKeyword(UUID bookId, String keyword) {
        BooleanBuilder condition = baseCondition(bookId, keyword);
        return queryFactory.select(r.count())
                .from(r)
                .leftJoin(r.user, u)
                .leftJoin(r.book, b)
                .where(condition)
                .fetchOne();
    }

    @Override
    public long countByKeyword(String keyword) {
        BooleanBuilder condition = new BooleanBuilder(r.deletedAt.isNull());

        if (keyword != null && !keyword.isEmpty()) {
            condition.and(
                    u.nickname.containsIgnoreCase(keyword)
                            .or(r.content.containsIgnoreCase(keyword))
                            .or(b.title.containsIgnoreCase(keyword))
            );
        }

        return queryFactory.select(r.count())
                .from(r)
                .leftJoin(r.user, u)
                .leftJoin(r.book, b)
                .where(condition)
                .fetchOne();
    }

    /** 공통 조건 생성기 **/
    private BooleanBuilder baseCondition(UUID bookId, String keyword) {
        BooleanBuilder condition = new BooleanBuilder(r.deletedAt.isNull());

        if (bookId != null) {
            condition.and(r.book.id.eq(bookId));
        }

        if (keyword != null && !keyword.isEmpty()) {
            condition.and(
                    u.nickname.containsIgnoreCase(keyword)
                            .or(r.content.containsIgnoreCase(keyword))
                            .or(b.title.containsIgnoreCase(keyword))
            );
        }

        return condition;
    }
}