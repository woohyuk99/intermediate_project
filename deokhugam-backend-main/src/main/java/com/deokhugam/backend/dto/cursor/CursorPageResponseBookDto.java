package com.deokhugam.backend.dto.cursor;

import com.deokhugam.backend.dto.book.BookDto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseBookDto(
        List<BookDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
