package com.deokhugam.backend.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewUpdateRequest(

        @NotBlank(message = "리뷰 내용은 비어있을 수 없습니다")
        String content,

        @NotNull
        @Min(1) @Max(5)
        Integer rating // int -> Integer 변경 (NotNull 어노테이션 때문에)
) {
}
