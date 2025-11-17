package com.deokhugam.backend.dto.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID userId,
        UUID reviewId,
        String reviewTitle,
        String content,
        boolean confirmed,
        Instant createdAt,
        Instant updatedAt
) {
}
