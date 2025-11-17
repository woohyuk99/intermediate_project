package com.deokhugam.backend.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReviewCreateRequest(

        @NotNull(message = "도서 ID는 필수입니다")
        UUID bookId,

        @NotNull(message = "사용자 ID는 필수입니다")
        UUID userId,

        @NotBlank(message = "리뷰 내용은 비어있을 수 없습니다")
        String content,

        @NotNull
        @Min(1) @Max(5)
        Integer rating
) {
}
