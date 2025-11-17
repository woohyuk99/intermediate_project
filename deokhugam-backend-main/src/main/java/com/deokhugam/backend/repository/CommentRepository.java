package com.deokhugam.backend.repository;

import com.deokhugam.backend.entity.Comment;
import com.deokhugam.backend.repository.query.CommentQueryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentQueryRepository {

    // 댓글 상세 정보 조회 API, 논리삭제되지 않은 댓글만 조회
    Optional<Comment> findByIdAndDeletedAtIsNull(UUID id);
}
