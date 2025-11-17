package com.deokhugam.backend.dto.cursor;

import com.deokhugam.backend.dto.comment.CommentDto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseCommentDto(
        List<CommentDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
