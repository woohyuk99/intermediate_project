package com.deokhugam.backend.repository;

import com.deokhugam.backend.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  // 모두 읽음 처리
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
    update Notification n
    set n.confirmed = true,
        n.confirmedAt = :now,
        n.updatedAt = :now
    where n.user.id = :userId
      and n.confirmed = false
  """)
  int markAllAsRead(@Param("userId") UUID userId, @Param("now") Instant now);

  // ====== 첫 페이지(내림차순) : after 없을 때 ======
  @Query("""
    select n
    from Notification n
    where n.user.id = :userId
    order by n.createdAt desc, n.id desc
  """)
  List<Notification> findFirstPageDescNoAfter(@Param("userId") UUID userId, Pageable pageable);

  // ====== 첫 페이지(내림차순) : after 있을 때 ======
  @Query("""
    select n
    from Notification n
    where n.user.id = :userId
      and n.createdAt <= :after
    order by n.createdAt desc, n.id desc
  """)
  List<Notification> findFirstPageDescWithAfter(@Param("userId") UUID userId,
      @Param("after") Instant after,
      Pageable pageable);

  // ====== 첫 페이지(오름차순) : after 없을 때 ======
  @Query("""
    select n
    from Notification n
    where n.user.id = :userId
    order by n.createdAt asc, n.id asc
  """)
  List<Notification> findFirstPageAscNoAfter(@Param("userId") UUID userId, Pageable pageable);

  // ====== 첫 페이지(오름차순) : after 있을 때 ======
  @Query("""
    select n
    from Notification n
    where n.user.id = :userId
      and n.createdAt >= :after
    order by n.createdAt asc, n.id asc
  """)
  List<Notification> findFirstPageAscWithAfter(@Param("userId") UUID userId,
      @Param("after") Instant after,
      Pageable pageable);

  // ====== 커서 이후 페이지(내림차순) ======
  @Query("""
    select n
    from Notification n
    where n.user.id = :userId
      and (n.createdAt < :cAt or (n.createdAt = :cAt and n.id < :cId))
    order by n.createdAt desc, n.id desc
  """)
  List<Notification> findAfterCursorDesc(@Param("userId") UUID userId,
      @Param("cAt") Instant createdAt,
      @Param("cId") UUID id,
      Pageable pageable);

  // ===== 커서 이후 페이지(오름차순) =====
  @Query("""
    select n
    from Notification n
    where n.user.id = :userId
      and (n.createdAt > :cAt or (n.createdAt = :cAt and n.id > :cId))
    order by n.createdAt asc, n.id asc
  """)
  List<Notification> findAfterCursorAsc(@Param("userId") UUID userId,
      @Param("cAt") Instant createdAt,
      @Param("cId") UUID id,
      Pageable pageable);
}
