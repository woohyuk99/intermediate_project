package com.deokhugam.backend.repository;

import com.deokhugam.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // 단일 조회 (soft delete 체크)
    @Query("SELECT r FROM Review r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Review> findById(@Param("id") UUID id);

    // 개수 조회 (soft delete 제외)
    @Query("SELECT COUNT(r) FROM Review r WHERE r.deletedAt IS NULL")
    long count();

    @Query("SELECT COUNT(r) FROM Review r WHERE r.book.id = :bookId AND r.deletedAt IS NULL")
    long countByBookId(@Param("bookId") UUID bookId);

    // 중복 체크 (soft delete 제외)
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r WHERE r.user.id = :userId AND r.book.id = :bookId AND r.deletedAt IS NULL")
    boolean existsByUserIdAndBookId(
            @Param("userId") UUID userId,
            @Param("bookId") UUID bookId
    );

    // 평균 쿼리
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId AND r.deletedAt IS NULL")
    Double avgRatingByBookId(@Param("bookId") UUID bookId);

}
