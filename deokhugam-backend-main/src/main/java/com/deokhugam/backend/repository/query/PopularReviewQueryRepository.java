package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.entity.Period;
import com.deokhugam.backend.entity.PopularReview;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface PopularReviewQueryRepository {
    List<PopularReview> findWithCursor(
            Period period,
            Double cursorScore,
            Instant cursorCreatedAt,
            Pageable pageable
    );

    long countByPeriod(Period period);
}