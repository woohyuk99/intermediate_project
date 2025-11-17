package com.deokhugam.backend.dto.cursor;

import com.deokhugam.backend.dto.notification.NotificationDto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseNotificationDto(
        List<NotificationDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
