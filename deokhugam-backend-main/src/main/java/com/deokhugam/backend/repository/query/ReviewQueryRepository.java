package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.entity.Review;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReviewQueryRepository {
    // createdAt 기준 정렬 + 커서 기반 조회
    List<Review> findWithCreatedAtSort(UUID bookId, Instant cursor, boolean isAsc, String keyword, Pageable pageable);

    // rating 기준 정렬 + 커서 기반 조회
    List<Review> findWithRatingSort(UUID bookId, Integer ratingCursor, Instant createdAtCursor, boolean isAsc, String keyword, Pageable pageable);

    // count 쿼리 (검색 포함)
    long countByBookIdAndKeyword(UUID bookId, String keyword);
    long countByKeyword(String keyword);
}
