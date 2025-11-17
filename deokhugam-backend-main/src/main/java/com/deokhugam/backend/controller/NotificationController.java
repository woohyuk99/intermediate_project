package com.deokhugam.backend.controller;

import com.deokhugam.backend.dto.cursor.CursorPageResponseNotificationDto;
import com.deokhugam.backend.dto.notification.NotificationDto;
import com.deokhugam.backend.dto.notification.NotificationUpdateRequest;
import com.deokhugam.backend.service.NotificationService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService service;

  //TODO TEST
  @PatchMapping("/{notificationId}")
  public NotificationDto updateRead(
      @PathVariable UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-Id") UUID userId,
      @Valid @RequestBody NotificationUpdateRequest request
  ) {
    return service.updateRead(userId, notificationId, request);
  }

  @PatchMapping("/read-all")
  public ResponseEntity<Void> readAll(
      @RequestHeader("Deokhugam-Request-User-Id") UUID userId
  ) {
    int updated = service.readAll(userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public CursorPageResponseNotificationDto list(
      @RequestParam UUID userId,
      @RequestParam(defaultValue = "DESC") Sort.Direction direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestParam(defaultValue = "20") int limit
  ) {
    return service.list(userId, direction, cursor, after, limit);
  }
}
