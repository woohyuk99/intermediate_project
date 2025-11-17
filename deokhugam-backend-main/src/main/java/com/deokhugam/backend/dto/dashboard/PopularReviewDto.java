package com.deokhugam.backend.dto.dashboard;

import java.time.Instant;
import java.util.UUID;

public record PopularReviewDto(
        UUID id,
        UUID reviewId,
        UUID bookId,
        String bookTitle,
        String bookThumbnailUrl,
        UUID userId,
        String userNickname,
        String reviewContent,
        double reviewRating,
        String period,
        Instant createdAt,
        long rank,
        double score,
        long likeCount,
        long commentCount
) {
}
