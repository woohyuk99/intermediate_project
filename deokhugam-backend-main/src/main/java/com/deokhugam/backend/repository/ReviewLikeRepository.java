package com.deokhugam.backend.repository;

import com.deokhugam.backend.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    @Query("SELECT CASE WHEN COUNT(rl) > 0 THEN true ELSE false END FROM ReviewLike rl WHERE rl.review.id = :reviewId AND rl.user.id = :userId AND rl.review.deletedAt IS NULL")
    boolean existsByReviewIdAndUserId(
            @Param("reviewId") UUID reviewId,
            @Param("userId") UUID userId
    );

    @Query("SELECT rl FROM ReviewLike rl WHERE rl.review.id = :reviewId AND rl.user.id = :userId AND rl.review.deletedAt IS NULL")
    Optional<ReviewLike> findByReviewIdAndUserId(
            @Param("reviewId") UUID reviewId,
            @Param("userId") UUID userId
    );

    @Query("SELECT rl.review.id FROM ReviewLike rl WHERE rl.user.id = :userId AND rl.review.id IN :reviewIds AND rl.review.deletedAt IS NULL")
    Set<UUID> findLikedReviewIdsByUserIdAndReviewIds(
            @Param("userId") UUID userId,
            @Param("reviewIds") List<UUID> reviewIds
    );}
