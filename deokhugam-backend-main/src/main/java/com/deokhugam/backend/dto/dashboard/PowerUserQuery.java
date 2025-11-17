package com.deokhugam.backend.dto.dashboard;

import com.deokhugam.backend.entity.Period;

import java.time.Instant;
import java.util.UUID;

import static com.deokhugam.backend.entity.Period.DAILY;

public record PowerUserQuery(
        Period period,
        String direction,
        UUID cursor,
        Instant after,
        Integer limit
) {
    public PowerUserQuery {
        if (direction == null) {
            direction = "ASC";
        }
        if (limit == null) {
            limit = 50;
        }
        if (period == null) {
            period = DAILY;
        }
    }
}
