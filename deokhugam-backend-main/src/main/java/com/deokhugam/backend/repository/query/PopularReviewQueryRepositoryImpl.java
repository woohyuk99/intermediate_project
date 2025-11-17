package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.entity.Period;
import com.deokhugam.backend.entity.PopularReview;
import com.deokhugam.backend.entity.QBook;
import com.deokhugam.backend.entity.QPopularReview;
import com.deokhugam.backend.entity.QReview;
import com.deokhugam.backend.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PopularReviewQueryRepositoryImpl implements PopularReviewQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularReview> findWithCursor(
            Period period,
            Double cursorScore,
            Instant cursorCreatedAt,
            Pageable pageable
    ) {
        QPopularReview pr = QPopularReview.popularReview;
        QReview r = QReview.review;
        QUser u = QUser.user;
        QBook b = QBook.book;

        var query = queryFactory
                .selectFrom(pr)
                .leftJoin(pr.review, r).fetchJoin()
                .leftJoin(r.user, u).fetchJoin()
                .leftJoin(r.book, b).fetchJoin()
                .where(pr.period.eq(period));

        // ✅ 커서 조건 추가 (null-safe)
        if (cursorScore != null && cursorCreatedAt != null) {
            query.where(
                    pr.score.lt(cursorScore)
                            .or(
                                    pr.score.eq(cursorScore)
                                            .and(r.createdAt.lt(cursorCreatedAt))
                            )
            );
        }

        // ✅ 정렬 및 페이징 (pageable의 size로 limit 적용)
        return query
                .orderBy(pr.score.desc(), r.createdAt.desc())
                .limit(pageable.getPageSize()) // Pageable의 size를 그대로 사용
                .fetch();
    }

    @Override
    public long countByPeriod(Period period) {
        QPopularReview pr = QPopularReview.popularReview;

        return queryFactory
                .select(pr.count())
                .from(pr)
                .where(pr.period.eq(period))
                .fetchOne();
    }
}
