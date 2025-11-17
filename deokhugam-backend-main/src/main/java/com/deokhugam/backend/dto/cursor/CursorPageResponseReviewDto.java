package com.deokhugam.backend.dto.cursor;

import com.deokhugam.backend.dto.review.ReviewDto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseReviewDto(
        List<ReviewDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
