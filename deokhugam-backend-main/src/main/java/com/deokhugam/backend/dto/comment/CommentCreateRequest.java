package com.deokhugam.backend.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentCreateRequest(

        @NotNull(message = "reviewId는 필수입니다.")
        UUID reviewId,

        @NotNull(message = "userId는 필수입니다.")
        UUID userId,

        @NotBlank(message = "content는 비어 있을 수 없습니다.")
        String content
) {
}
