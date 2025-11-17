package com.deokhugam.backend.service;

import com.deokhugam.backend.dto.cursor.CursorPageResponseNotificationDto;
import com.deokhugam.backend.dto.notification.NotificationDto;
import com.deokhugam.backend.dto.notification.NotificationUpdateRequest;
import com.deokhugam.backend.entity.Comment;
import com.deokhugam.backend.entity.Notification;
import com.deokhugam.backend.entity.Review;
import com.deokhugam.backend.entity.User;
import com.deokhugam.backend.entity.Period;
import com.deokhugam.backend.mapper.NotificationMapper;
import com.deokhugam.backend.repository.CommentRepository;
import com.deokhugam.backend.repository.NotificationRepository;
import com.deokhugam.backend.repository.ReviewRepository;
import com.deokhugam.backend.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository repository;
  private final NotificationMapper mapper;
  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;

  //TODO TEST
  public NotificationDto getById(UUID id) {
    Notification notification = repository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Notification with id " + id + " not found"));
    return mapper.toDto(notification);
  }

  @Transactional
  public NotificationDto updateRead(UUID userId, UUID notificationId, NotificationUpdateRequest request) {
    Notification n = repository.findById(notificationId)
        .orElseThrow(() -> new NoSuchElementException("Notification with id " + notificationId + " not found"));

    if (!n.getUser().getId().equals(userId)) {
      throw new RuntimeException("Access denied");
    }

    n.update(request);
    if (n.isConfirmed()) {
      setConfirmedAt(n, Instant.now());
    } else {
      setConfirmedAt(n, null);
    }
    return mapper.toDto(n);
  }

  private void setConfirmedAt(Notification n, Instant confirmedAt) {
    try {
      var f = n.getClass().getDeclaredField("confirmedAt");
      f.setAccessible(true);
      f.set(n, confirmedAt);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Transactional
  public int readAll(UUID userId) {
    return repository.markAllAsRead(userId, Instant.now());
  }

  @Transactional(readOnly = true)
  public CursorPageResponseNotificationDto list(
      UUID userId,
      Sort.Direction direction,
      String cursor,
      Instant after,
      int limit
  ) {
    int size = Math.max(1, Math.min(limit, 100));
    PageRequest pageable = PageRequest.of(0, size);

    List<Notification> rows;

    Instant cursorCreatedAt = null;
    UUID cursorId = null;
    if (cursor != null && !cursor.isBlank()) {
      String[] parts = cursor.split("\\|");
      if (parts.length == 2) {
        cursorCreatedAt = Instant.parse(parts[0]);
        cursorId = UUID.fromString(parts[1]);
      }
    }

    if (cursorCreatedAt != null && cursorId != null) {
      // 커서가 있으면 커서 기반
      if (direction == Sort.Direction.ASC) {
        rows = repository.findAfterCursorAsc(userId, cursorCreatedAt, cursorId, pageable);
      } else {
        rows = repository.findAfterCursorDesc(userId, cursorCreatedAt, cursorId, pageable);
      }
    } else {
      // 커서가 없으면 첫 페이지. after가 null인지에 따라 쿼리 분리
      if (direction == Sort.Direction.ASC) {
        rows = (after == null)
            ? repository.findFirstPageAscNoAfter(userId, pageable)
            : repository.findFirstPageAscWithAfter(userId, after, pageable);
      } else {
        rows = (after == null)
            ? repository.findFirstPageDescNoAfter(userId, pageable)
            : repository.findFirstPageDescWithAfter(userId, after, pageable);
      }
    }

    List<NotificationDto> content = rows.stream()
        .map(mapper::toDto)
        .toList();

    boolean hasNext = content.size() == size;
    String nextCursor = null;
    Instant nextAfter = null;

    if (!content.isEmpty()) {
      Notification tail = rows.get(rows.size() - 1);
      nextCursor = tail.getCreatedAt() + "|" + tail.getId();
      nextAfter = tail.getCreatedAt();
    }

    return new CursorPageResponseNotificationDto(
        content,
        nextCursor,
        nextAfter,
        content.size(),
        content.size(),
        hasNext
    );
  }

  @Transactional
  public void createLikeNotification(UUID reviewId, UUID actorUserId, UUID targetUserId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new NoSuchElementException("Review not found: " + reviewId));

    UUID actualTargetUserId = (targetUserId != null) ? targetUserId : review.getUser().getId();

    if (actorUserId.equals(actualTargetUserId)) {
      return;
    }

    User targetUser = userRepository.findById(actualTargetUserId)
        .orElseThrow(() -> new NoSuchElementException("User not found: " + actualTargetUserId));

    User actorUser = userRepository.findById(actorUserId)
        .orElseThrow(() -> new NoSuchElementException("User not found: " + actorUserId));

    String content = String.format("[%s]님이 나의 리뷰를 좋아합니다.", actorUser.getNickname());

    Notification notification = Notification.builder()
        .user(targetUser)
        .review(review)
        .actorUser(actorUser)
        .content(content)
        .confirmed(false)
        .build();

    repository.save(notification);
  }

  @Transactional
  public void createCommentNotification(UUID reviewId, UUID commentId, UUID actorUserId, UUID targetUserId) {
    if (actorUserId.equals(targetUserId)) {
      return;
    }

    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new NoSuchElementException("Review not found: " + reviewId));

    User targetUser = userRepository.findById(targetUserId)
        .orElseThrow(() -> new NoSuchElementException("User not found: " + targetUserId));

    User actorUser = userRepository.findById(actorUserId)
        .orElseThrow(() -> new NoSuchElementException("User not found: " + actorUserId));

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new NoSuchElementException("Comment not found: " + commentId));

    String content = String.format("[%s]님이 나의 리뷰에 댓글을 남겼습니다.", actorUser.getNickname());

    Notification notification = Notification.builder()
        .user(targetUser)
        .review(review)
        .actorUser(actorUser)
        .comment(comment)
        .content(content)
        .confirmed(false)
        .build();

    repository.save(notification);
  }

  @Transactional
  public void createPopularReviewNotification(UUID reviewId, UUID targetUserId, Period period, Long rank) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new NoSuchElementException("Review not found: " + reviewId));

    User targetUser = userRepository.findById(targetUserId)
        .orElseThrow(() -> new NoSuchElementException("User not found: " + targetUserId));

    String periodName = switch (period) {
      case DAILY -> "일간";
      case WEEKLY -> "주간";
      case MONTHLY -> "월간";
      case ALL_TIME -> "전체기간";
    };

    String content = String.format("내가 작성한 리뷰가 %s 인기 리뷰 %d위에 선정되었습니다!", periodName, rank);

    Notification notification = Notification.builder()
        .user(targetUser)
        .review(review)
        .content(content)
        .confirmed(false)
        .build();

    repository.save(notification);
  }
}
