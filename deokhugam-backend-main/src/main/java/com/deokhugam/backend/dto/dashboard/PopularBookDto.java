package com.deokhugam.backend.dto.dashboard;

import java.time.Instant;
import java.util.UUID;

public record PopularBookDto(
        UUID id,
        UUID bookId,
        String title,
        String author,
        String thumbnailUrl,
        String period,
        long rank,
        double score,
        long reviewCount,
        double rating,
        Instant createdAt
) {
}
