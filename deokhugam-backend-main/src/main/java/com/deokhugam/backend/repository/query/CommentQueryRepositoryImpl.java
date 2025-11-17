package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.dto.comment.CommentDto;
import com.deokhugam.backend.dto.cursor.CursorPageResponseCommentDto;
import com.deokhugam.backend.entity.Comment;
import com.deokhugam.backend.entity.QComment;
import com.deokhugam.backend.entity.QReview;
import com.deokhugam.backend.entity.QUser;
import com.deokhugam.backend.mapper.CommentMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final CommentMapper commentMapper;

    private static final QComment c = QComment.comment;
    private static final QReview r = QReview.review;
    private static final QUser u = QUser.user;

    @Override
    public CursorPageResponseCommentDto findByReviewIdWithCursor(
            UUID reviewId,
            Sort.Direction direction,
            UUID cursor,
            Instant after,
            int limit
    ) {
        Sort.Direction dir = (direction != null) ? direction : Sort.Direction.DESC;
        int size = (limit > 0) ? limit : 20;

        BooleanBuilder where = new BooleanBuilder();
        where.and(c.deletedAt.isNull());
        where.and(r.deletedAt.isNull());
        where.and(r.id.eq(reviewId));

        // 커서 기반 조건 (복합키: createdAt, id)
        if (after != null && cursor != null) {
            if (dir == Sort.Direction.DESC) {
                where.and(c.createdAt.lt(after)
                        .or(c.createdAt.eq(after).and(c.id.lt(cursor))));
            } else {
                where.and(c.createdAt.gt(after)
                        .or(c.createdAt.eq(after).and(c.id.gt(cursor))));
            }
        }

        // 정렬 조건
        OrderSpecifier<?>[] order = (dir == Sort.Direction.DESC)
                ? new OrderSpecifier[]{c.createdAt.desc(), c.id.desc()}
                : new OrderSpecifier[]{c.createdAt.asc(), c.id.asc()};

        // 실제 조회 (fetch join으로 N+1 방지)
        List<Comment> rows = queryFactory
                .selectFrom(c)
                .join(c.user, u).fetchJoin()
                .join(c.review, r).fetchJoin()
                .where(where)
                .orderBy(order)
                .limit(size + 1L)
                .fetch();

        boolean hasNext = rows.size() > size;
        // if (hasNext) rows = new ArrayList<>(rows.subList(0, size));
        if (hasNext) {rows.remove(rows.size() - 1);}

        // 총 개수 (논리삭제 제외)
        Long totalCount = queryFactory
                .select(c.count())
                .from(c)
                .join(c.review, r)
                .where(c.deletedAt.isNull()
                        .and(r.deletedAt.isNull())
                        .and(r.id.eq(reviewId)))
                .fetchOne();

        long totalElements = (totalCount != null) ? totalCount : 0L;

        // DTO 변환
        List<CommentDto> content = rows.stream()
                .map(commentMapper::toDto)
                .toList();


        // nextCursor / nextAfter
        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !rows.isEmpty()) {
            Comment last = rows.get(rows.size() - 1);
            nextCursor = last.getId().toString();
            nextAfter = last.getCreatedAt();
        }

        return new CursorPageResponseCommentDto(
                content,
                nextCursor,
                nextAfter,
                size,
                totalElements,
                hasNext
        );
    }
}
