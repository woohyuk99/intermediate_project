package com.deokhugam.backend.service;

import com.deokhugam.backend.dto.cursor.CursorPageResponsePopularReviewDto;
import com.deokhugam.backend.dto.cursor.CursorPageResponseReviewDto;
import com.deokhugam.backend.dto.dashboard.PopularReviewDto;
import com.deokhugam.backend.dto.review.ReviewCreateRequest;
import com.deokhugam.backend.dto.review.ReviewDto;
import com.deokhugam.backend.dto.review.ReviewLikeDto;
import com.deokhugam.backend.dto.review.ReviewUpdateRequest;
import com.deokhugam.backend.entity.*;
import com.deokhugam.backend.mapper.PopularReviewMapper;
import com.deokhugam.backend.mapper.ReviewMapper;
import com.deokhugam.backend.repository.*;
import com.deokhugam.backend.repository.query.PopularReviewQueryRepository;
import com.deokhugam.backend.repository.query.ReviewQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewLikeRepository reviewLikeRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final UserRepository userRepository;
    private final PopularReviewQueryRepository popularReviewQueryRepository;
    private final NotificationService notificationService;
    private final BookService bookService;
    private final ReviewMapper reviewMapper;
    private final PopularReviewMapper popularReviewMapper;


    // 리뷰 목록 조회 (Cursor 기반)
    @Transactional(readOnly = true)
    public CursorPageResponseReviewDto findReviews(
            String cursor,
            int size,
            UUID bookId,
            String orderBy,
            String direction,
            String keyword,
            UUID userId
    ) {
        // 정렬 기준 및 방향 설정 (기본값: createdAt, DESC)
        String sortField = (orderBy != null && orderBy.equalsIgnoreCase("rating")) ? "rating" : "createdAt";
        boolean isAsc = direction != null && direction.equalsIgnoreCase("ASC");

        // 커서 파싱 (복합 커서: "주정렬값,createdAt" 형태)
        Integer ratingCursor = null;
        Instant createdAtCursor = null;

        if (cursor != null && !cursor.isEmpty()) {
            String[] parts = cursor.split(",");
            if (sortField.equals("rating")) {
                ratingCursor = Integer.parseInt(parts[0]);
                createdAtCursor = parts.length > 1 ? Instant.parse(parts[1]) : null;
            } else {
                createdAtCursor = Instant.parse(parts[0]);
            }
        }

        // 커서 기반 조회
        List<Review> reviews;
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        if (sortField.equals("rating")) {
            reviews = reviewQueryRepository.findWithRatingSort(
                    bookId, ratingCursor, createdAtCursor, isAsc, keyword, pageRequest
            );
        } else {
            reviews = reviewQueryRepository.findWithCreatedAtSort(
                    bookId, createdAtCursor, isAsc, keyword, pageRequest
            );
        }

        // hasNext 판단을 위해 size + 1개 조회
        boolean hasNext = reviews.size() > size;
        if (hasNext) {
            reviews = reviews.subList(0, size);
        }

        // 좋아요 정보 조회
        List<ReviewDto> reviewDtos;
        if (userId != null) {
            List<UUID> reviewIds = reviews.stream()
                    .map(Review::getId)
                    .toList();
            Set<UUID> likedReviewIds = reviewLikeRepository
                    .findLikedReviewIdsByUserIdAndReviewIds(userId, reviewIds);

            reviewDtos = reviews.stream()
                    .map(review -> reviewMapper.toReviewDto(
                            review,
                            likedReviewIds.contains(review.getId())
                    ))
                    .toList();
        } else {
            reviewDtos = reviews.stream()
                    .map(review -> reviewMapper.toReviewDto(review, false))
                    .toList();
        }

        // 다음 커서 생성 (복합 커서)
        String nextCursor = null;
        Instant nextAfter = null;
        if (hasNext && !reviews.isEmpty()) {
            Review lastReview = reviews.get(reviews.size() - 1);
            nextAfter = lastReview.getCreatedAt();

            if (sortField.equals("rating")) {
                // "rating,createdAt" 형태
                nextCursor = lastReview.getRating() + "," + nextAfter.toString();
            } else {
                // "createdAt" 형태
                nextCursor = nextAfter.toString();
            }
        }

        // 전체 개수 조회
        long totalElements;
        if (keyword != null && !keyword.trim().isEmpty()) {
            totalElements = bookId != null
                    ? reviewQueryRepository.countByBookIdAndKeyword(bookId, keyword)
                    : reviewQueryRepository.countByKeyword(keyword);
        } else {
            totalElements = bookId != null
                    ? reviewRepository.countByBookId(bookId)
                    : reviewRepository.count();
        }

        return new CursorPageResponseReviewDto(
                reviewDtos,
                nextCursor,
                nextAfter,
                reviewDtos.size(),
                totalElements,
                hasNext
        );
    }


    // 리뷰 생성
    @Transactional
    public ReviewDto create(ReviewCreateRequest req) {
        // 중복 리뷰 체크 (soft delete 제외)
        if (reviewRepository.existsByUserIdAndBookId(req.userId(), req.bookId())) {
            throw new IllegalStateException("이미 해당 도서에 대한 리뷰를 작성하셨습니다.");
        }

        Book book = bookRepository.findById(req.bookId())
                .orElseThrow(() -> new NoSuchElementException("도서를 찾을 수 없습니다."));
        User user = userRepository.findById(req.userId())
                .orElseThrow(()->new NoSuchElementException("사용자를 찾을 수 없습니다."));

        Review review = reviewMapper.toReview(req, book, user);
        reviewRepository.save(review);

        // ✅ 리뷰 생성 후 통계 반영
        bookService.refreshForBook(req.bookId());
        return reviewMapper.toReviewDto(review, false);
    }

    // 인기 리뷰 목록 조회 (Period 기반)
    @Transactional(readOnly = true)
    public CursorPageResponsePopularReviewDto findPopularReviews(
            String cursor,
            int size,
            Period period,
            String direction,
            UUID userId
    ) {
        // 커서 파싱: "score,createdAt" 형식
        Double cursorScore = null;
        Instant cursorCreatedAt = null;

        if (cursor != null && !cursor.isEmpty()) {
            String[] parts = cursor.split(",");
            cursorScore = Double.parseDouble(parts[0]);
            cursorCreatedAt = parts.length > 1 ? Instant.parse(parts[1]) : null;
        }

        // PopularReview 조회 (내림차순 고정)
        List<PopularReview> popularReviews;
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        popularReviews = popularReviewQueryRepository.findWithCursor(
                period, cursorScore, cursorCreatedAt, pageRequest
        );

        // hasNext 판단
        boolean hasNext = popularReviews.size() > size;
        if (hasNext) {
            popularReviews = popularReviews.subList(0, size);
        }

        // Review ID 추출
        List<Review> reviews = popularReviews.stream()
                .map(PopularReview::getReview)
                .toList();

        // 좋아요 정보 조회
        List<PopularReviewDto> popularReviewDtos;

        if (userId != null) {
            List<UUID> reviewIds = reviews.stream()
                    .map(Review::getId)
                    .toList();
            Set<UUID> likedReviewIds = reviewLikeRepository
                    .findLikedReviewIdsByUserIdAndReviewIds(userId, reviewIds);

            popularReviewDtos = popularReviews.stream()
                    .map(popularReview -> popularReviewMapper.toPopularReviewDto(
                            popularReview,
                            likedReviewIds.contains(popularReview.getReview().getId())
                    ))
                    .toList();
        } else {
            popularReviewDtos = popularReviews.stream()
                    .map(popularReview -> popularReviewMapper.toPopularReviewDto(popularReview, false))
                    .toList();
        }

        // 다음 커서 생성
        String nextCursor = null;
        Instant nextAfter = null;
        if (hasNext && !popularReviews.isEmpty()) {
            PopularReview lastPopularReview = popularReviews.get(popularReviews.size() - 1);
            nextAfter = lastPopularReview.getReview().getCreatedAt();
            // "score,createdAt" 형식으로 커서 생성
            nextCursor = lastPopularReview.getScore() + "," + nextAfter.toString();
        }

        // 전체 개수 조회
        long totalElements = popularReviewQueryRepository.countByPeriod(period);

        return new CursorPageResponsePopularReviewDto(
                popularReviewDtos,
                nextCursor,
                nextAfter,
                popularReviewDtos.size(),
                totalElements,
                hasNext
        );
    }

    // 리뷰 상세 조회
    @Transactional(readOnly = true)
    public ReviewDto findById(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("review not found: " + reviewId));

        boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId);

        return reviewMapper.toReviewDto(review, likedByMe);
    }

    // 리뷰 수정
    @Transactional
    public ReviewDto update(UUID reviewId, ReviewUpdateRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("review not found: " + reviewId));
        review.update(req);


        bookService.refreshForBook(review.getBook().getId());

        boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, review.getUser().getId());
        return reviewMapper.toReviewDto(review, likedByMe);
    }

    // 리뷰 논리 삭제
    @Transactional
    public void softDelete(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("review not found: " + reviewId));
        UUID bookId = review.getBook().getId();

        review.softDelete();

        bookService.refreshForBook(bookId);
    }

    // 리뷰 물리 삭제
    @Transactional
    public void hardDelete(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("review not found: " + reviewId));
        UUID bookId = review.getBook().getId();

        reviewRepository.deleteById(reviewId);

        bookService.refreshForBook(bookId);
    }

    // 리뷰 좋아요
    @Transactional
    public ReviewLikeDto likeReview(UUID reviewId, UUID userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NoSuchElementException("리뷰를 찾을 수 없습니다: " + reviewId));

//        UUID userId = review.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        Optional<ReviewLike> existingLike = reviewLikeRepository
                .findByReviewIdAndUserId(reviewId, userId);

        boolean liked;
        if (existingLike.isPresent()) {
            // 좋아요 취소
            reviewLikeRepository.delete(existingLike.get());
            review.decrementLikeCount();
            liked = false;
        } else {
            // 좋아요 추가
            ReviewLike reviewLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build();
            reviewLikeRepository.save(reviewLike);
            review.incrementLikeCount();
            liked = true;
            
            // 알림 생성 (본인이 아니면)
            if (!review.getUser().getId().equals(userId)) {
                notificationService.createLikeNotification(reviewId, userId, review.getUser().getId());
            }
        }

        return new ReviewLikeDto(reviewId, userId, liked);
    }
}
