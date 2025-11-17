package com.deokhugam.backend.repository;

import com.deokhugam.backend.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> { // Book 엔티티용 JPA 리포지토리 인터페이스 선언
    boolean existsByIsbn(String isbn); // ISBN 중복 여부를 확인하는 메서드 시그니처 선언
    Optional<Book> findByIdAndDeletedAtIsNull(UUID id); // 논리 삭제되지 않은 도서를 조회하는 메서드 시그니처 선언

    // 리뷰 평균 평점, 리뷰 개수 업데이트
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Book b SET b.rating = :rating, b.reviewCount = :count WHERE b.id = :bookId")
    int updateStats(@Param("bookId") UUID bookId,
                    @Param("rating") double rating,
                    @Param("count") int count);
}
