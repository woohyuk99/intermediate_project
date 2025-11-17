package com.deokhugam.backend.dto.cursor;

import com.deokhugam.backend.dto.dashboard.PopularBookDto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponsePopularBookDto(
        List<PopularBookDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
