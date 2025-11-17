package com.deokhugam.backend.dto.cursor;

import com.deokhugam.backend.dto.dashboard.PowerUserDto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponsePowerUserDto(
        List<PowerUserDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {
}
