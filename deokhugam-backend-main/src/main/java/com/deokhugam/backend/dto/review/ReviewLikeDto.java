package com.deokhugam.backend.dto.review;

import java.util.UUID;

public record ReviewLikeDto(
        UUID reviewId,
        UUID userId,
        boolean liked
) {
}
