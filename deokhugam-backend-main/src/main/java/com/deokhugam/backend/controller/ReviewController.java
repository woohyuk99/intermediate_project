package com.deokhugam.backend.controller;

import com.deokhugam.backend.dto.cursor.CursorPageResponsePopularReviewDto;
import com.deokhugam.backend.dto.cursor.CursorPageResponseReviewDto;
import com.deokhugam.backend.dto.review.ReviewCreateRequest;
import com.deokhugam.backend.dto.review.ReviewDto;
import com.deokhugam.backend.dto.review.ReviewLikeDto;
import com.deokhugam.backend.dto.review.ReviewUpdateRequest;
import com.deokhugam.backend.entity.Period;
import com.deokhugam.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰 목록 조회
    @GetMapping
    public ResponseEntity<CursorPageResponseReviewDto> findReviews (
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID bookId,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String keyword,
            @RequestHeader(value = "deokhugam-request-user-id", required = false) UUID userId
    ) {
        CursorPageResponseReviewDto reviews = reviewService.findReviews(
                cursor, size, bookId, orderBy, direction, keyword, userId
        );
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 등록
    @PostMapping
    public ResponseEntity<ReviewDto> create (
            @Valid @RequestBody ReviewCreateRequest req
    ) {
        ReviewDto created = reviewService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 리뷰 좋아요
    @PostMapping("/{reviewId}/like")
    public ResponseEntity<ReviewLikeDto> likeReview (
            @PathVariable UUID reviewId,
            @RequestHeader(value = "deokhugam-request-user-id", required = false) UUID userId
    ) {
        ReviewLikeDto liked = reviewService.likeReview(reviewId, userId);
        return ResponseEntity.ok(liked);
    }

    // 리뷰 상세 정보 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> findById (
            @PathVariable UUID reviewId,
            @RequestHeader(value = "deokhugam-request-user-id", required = false) UUID userId
    ) {
        ReviewDto review = reviewService.findById(reviewId, userId);
        return ResponseEntity.ok(review);
    }

    // 리뷰 논리 삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> delete (
            @PathVariable UUID reviewId
    ) {
        reviewService.softDelete(reviewId);
        return ResponseEntity.noContent().build();
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> update (
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest req
    ) {
        ReviewDto updated = reviewService.update(reviewId, req);
        return ResponseEntity.ok(updated);
    }

    // 인기 리뷰 목록 조회
    @GetMapping("/popular")
    public ResponseEntity<CursorPageResponsePopularReviewDto> findPopularReviews (
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ALL_TIME") Period period,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestHeader(value = "deokhugam-request-user-id", required = false) UUID userId
    ) {
        CursorPageResponsePopularReviewDto popularReviews = reviewService.findPopularReviews(
                cursor, size, period, direction, userId
        );
        return ResponseEntity.ok(popularReviews);
    }

    // 리뷰 물리 삭제
    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> hardDelete (
            @PathVariable UUID reviewId
    ) {
        reviewService.hardDelete(reviewId);
        return ResponseEntity.noContent().build();
    }
}
