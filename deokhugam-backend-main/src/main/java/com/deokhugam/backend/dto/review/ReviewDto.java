package com.deokhugam.backend.dto.review;

import java.time.Instant;
import java.util.UUID;

public record ReviewDto(
        UUID id,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String content,
        int rating,
        int likeCount,
        int commentCount,
        boolean likedByMe,
        Instant createdAt,
        Instant updatedAt
) {
}
