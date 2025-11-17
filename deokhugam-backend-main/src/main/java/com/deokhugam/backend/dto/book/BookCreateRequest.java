package com.deokhugam.backend.dto.book;

import java.time.LocalDate;

public record BookCreateRequest(
        String title,
        String author,
        String description,
        String publisher,
        LocalDate publishedDate,
        String isbn
) {
}
