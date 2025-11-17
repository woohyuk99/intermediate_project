package com.deokhugam.backend.dto.cursor;

import com.deokhugam.backend.dto.dashboard.PopularReviewDto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponsePopularReviewDto(
        List<PopularReviewDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
