package com.deokhugam.backend.service;

import com.deokhugam.backend.dto.comment.CommentCreateRequest;
import com.deokhugam.backend.dto.comment.CommentDto;
import com.deokhugam.backend.dto.comment.CommentUpdateRequest;
import com.deokhugam.backend.dto.cursor.CursorPageResponseCommentDto;
import com.deokhugam.backend.entity.Comment;
import com.deokhugam.backend.entity.Review;
import com.deokhugam.backend.entity.User;
import com.deokhugam.backend.mapper.CommentMapper;
import com.deokhugam.backend.repository.CommentRepository;
import com.deokhugam.backend.repository.ReviewRepository;
import com.deokhugam.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;

    // 엔티티 매니저
    @PersistenceContext
    private EntityManager em; // review.commentCount 원자 증가용 (Repo 커스텀 없이 처리)

    // 댓글 생성
    @Transactional
    public CommentDto create(UUID requesterId, CommentCreateRequest request) {

        // 헤더의 사용자 ID와 바디의 userId가 동일해야 함 (보안상 이중 검증)
        if (!requesterId.equals(request.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "요청자와 작성자가 일치하지 않습니다.");
        }

        // 본문 최소 검증 (trim은 매퍼에서 수행하지만, 빈 문자열은 여기서 차단)
        String raw = request.content();
        String normalized = raw == null ? "" : raw.trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용은 비어 있을 수 없습니다.");
        }

        // 리뷰 조회 (기본 findById + 논리삭제 수동 필터)
        Review review = reviewRepository.findById(request.reviewId())
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        // 사용자 조회 (기본 findById + 논리삭제 수동 필터)
        User user = userRepository.findById(request.userId())
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));


        // 매퍼로 엔티티 생성 (빌더 내부 사용)
        Comment comment = commentMapper.toEntity(request, user, review);

        // 저장
        Comment saved = commentRepository.save(comment); // DB에 insert

        // 집계: review.commentCount 원자적 +1 (CRUD만 쓸 수 있어 JPQL로 처리)
        int updated = em.createQuery(
                        "update Review r set r.commentCount = r.commentCount + 1 where r.id = :id") // comment_count += 1
                .setParameter("id", review.getId()) // 파라미터 바인딩
                .executeUpdate();                   // 벌크 업데이트 실행

        // 업데이트가 1건이 아니면 비정상으로 판단
        if (updated != 1) { // 동시성/상태 이상 등
            throw new ResponseStatusException(HttpStatus.CONFLICT, "리뷰 집계 업데이트에 실패했습니다."); // 409 반환
        }

        // 알림 생성 (본인이 아니면) ->
        if (!review.getUser().getId().equals(user.getId())) {
            notificationService.createCommentNotification(review.getId(), saved.getId(), user.getId(), review.getUser().getId());
        }

        // 엔티티 → DTO 변환 후 반환
        return commentMapper.toDto(saved);
    }

    // 댓글 상세 정보 조회
    @Transactional(readOnly = true)
    public CommentDto getCommentById(UUID commentId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        return commentMapper.toDto(comment);
    }

    // 댓글 수정
    @Transactional
    public CommentDto updateComment(UUID commentId, UUID requesterId, CommentUpdateRequest request) {

        // 활성 댓글만 조회
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        // 권한 체크 (작성자 본인만)
        if (!comment.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 수정 권한이 없습니다.");
        }

        // 내용 정규화 + 유효성 검사 (엔티티는 그대로, 서비스에서 처리)
        String normalized = request.content() == null ? "" : request.content().trim();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용은 비어 있을 수 없습니다.");
        }

        // 변경 없으면 no-op (updatedAt도 그대로 유지)
        if (normalized.equals(comment.getContent())) {
            return commentMapper.toDto(comment);
        }

        // 엔티티의 기존 도메인 메서드 사용 (엔티티 수정 불필요)
        comment.update(new CommentUpdateRequest(normalized));

        // 더티체킹으로 content 변경됨 → Auditing이 updatedAt 자동 갱신
        return commentMapper.toDto(comment);
    }

    // 논리 삭제 (softDelete)
    @Transactional
    public void softDelete(UUID commentId, UUID requesterId) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다.");
        }

        comment.softDelete(); // BaseSoftDeletableEntity의 공통 메서드 호출
        // 집계: commentCount 1 감소 (원자적 감소; 0 이하로 내려가는 것 방지)

        UUID reviewId = comment.getReview().getId();
        em.createQuery(
                        "update Review r set r.commentCount = r.commentCount - 1 " +
                                "where r.id = :id and r.commentCount > 0")
                .setParameter("id", reviewId)
                .executeUpdate();
    }

    // 물리 삭제 (hardDelete)
    @Transactional
    public void hardDelete(UUID commentId, UUID requesterId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);



        // 리팩토링 참고:
        // - Notification.comment_id 가 FK ON DELETE SET NULL 이면 DB가 자동 반영
        // - Review/Book 등 연쇄는 설계대로 동작 (여기선 Comment만 물리삭제)
    }

    /**
     * 리뷰별 댓글 목록 조회
     * - direction: DESC(기본)
     * - cursor, after: optional
     * - limit: 기본 20
     */
    @Transactional(readOnly = true)
    public CursorPageResponseCommentDto getCommentsByReview(
            UUID reviewId,
            String direction,
            String cursor,
            Instant after,
            Integer limit
    ) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        UUID cursorId = (cursor != null && !cursor.isBlank())
                ? UUID.fromString(cursor)
                : null;

        int size = (limit != null && limit > 0) ? limit : 20;

        return commentRepository.findByReviewIdWithCursor(
                reviewId, sortDirection, cursorId, after, size
        );
    }



}
