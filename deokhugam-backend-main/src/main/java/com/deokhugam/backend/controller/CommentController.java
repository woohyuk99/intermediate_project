package com.deokhugam.backend.controller;

import com.deokhugam.backend.dto.comment.CommentCreateRequest;
import com.deokhugam.backend.dto.comment.CommentDto;
import com.deokhugam.backend.dto.comment.CommentUpdateRequest;
import com.deokhugam.backend.dto.cursor.CursorPageResponseCommentDto;
import com.deokhugam.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    public final CommentService commentService;

    // 댓글 등록
    @PostMapping
    public ResponseEntity<CommentDto> create(

            // 요구사항명세: 로그인에 성공한 사용자의 모든 요청 헤더(Deokhugam-Request-User-ID)에 사용자의 ID가 포함됩니다.
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId,
            @Valid @RequestBody CommentCreateRequest request
    ) {

        CommentDto saved = commentService.create(requesterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 댓글 상세 정보 조회
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getCommentById(@PathVariable UUID commentId) {
        CommentDto comment = commentService.getCommentById(commentId);
        return ResponseEntity.ok(comment);
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId,
            @PathVariable UUID commentId,
            @RequestBody CommentUpdateRequest request
    ) {
        CommentDto updated = commentService.updateComment(commentId, requesterId, request);
        return ResponseEntity.status(HttpStatus.OK).body(updated);
    }

    // 논리 삭제 (softDelete)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> softDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId,
            @PathVariable UUID commentId
    ) {
        commentService.softDelete(commentId, requesterId);
        return ResponseEntity.noContent().build();
    }

    // 물리 삭제 (hardDelete)
    @DeleteMapping("/{commentId}/hard")
    public ResponseEntity<Void> hardDelete(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId,
            @PathVariable UUID commentId
    ) {
        commentService.hardDelete(commentId, requesterId);
        return ResponseEntity.noContent().build();
    }

    // 리뷰 댓글 목록 조회
    @GetMapping
    public ResponseEntity<CursorPageResponseCommentDto> getCommentsByReview(
            @RequestParam("reviewId") UUID reviewId,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "after", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit
    ) {
        CursorPageResponseCommentDto response =
                commentService.getCommentsByReview(reviewId, direction, cursor, after, limit);
        return ResponseEntity.ok(response);
    }
}
