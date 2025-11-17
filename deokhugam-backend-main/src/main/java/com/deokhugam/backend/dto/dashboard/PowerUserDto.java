package com.deokhugam.backend.dto.dashboard;

import java.time.Instant;
import java.util.UUID;

public record PowerUserDto(
        UUID userId,
        String nickname,
        String period,
        Instant createdAt,
        long rank,
        double score,
        double reviewScoreSum,
        long likeCount,
        long commentCount
) {
}
