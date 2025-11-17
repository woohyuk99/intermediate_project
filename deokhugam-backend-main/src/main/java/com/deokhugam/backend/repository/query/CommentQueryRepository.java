// src/main/java/com/deokhugam/backend/repository/query/CommentQueryRepository.java
package com.deokhugam.backend.repository.query;

import com.deokhugam.backend.dto.cursor.CursorPageResponseCommentDto;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.UUID;

public interface CommentQueryRepository {
    CursorPageResponseCommentDto findByReviewIdWithCursor(
            UUID reviewId,
            Sort.Direction direction,   // ASC/DESC (기본 DESC)
            UUID cursor,                // 마지막 id
            Instant after,              // 마지막 createdAt
            int limit                   // 페이지 크기(기본 50)
    );
}
