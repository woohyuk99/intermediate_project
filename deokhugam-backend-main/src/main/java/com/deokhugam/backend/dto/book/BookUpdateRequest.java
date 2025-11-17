package com.deokhugam.backend.dto.book;

import java.time.LocalDate;

public record BookUpdateRequest(
        String title,
        String author,
        String description,
        String publisher,
        LocalDate publishedDate
) {
}
