package com.deokhugam.backend.entity;

import com.deokhugam.backend.dto.book.BookUpdateRequest;
import com.deokhugam.backend.entity.base.BaseSoftDeletableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Entity
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Book extends BaseSoftDeletableEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column(unique = true)
    private String isbn;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "rating")
    private Double rating = 0.0;

    public void update(BookUpdateRequest request) {
        if (!Objects.equals(this.title, request.title())) {
            this.title = request.title();
        }
        if (!Objects.equals(this.author, request.author())) {
            this.author = request.author();
        }
        if (!Objects.equals(this.description, request.description())) {
            this.description = request.description();
        }
        if (!Objects.equals(this.publisher, request.publisher())) {
            this.publisher = request.publisher();
        }
        if (!Objects.equals(this.publishedDate, request.publishedDate())) {
            this.publishedDate = request.publishedDate();
        }
    }

    public void updateThumbnailUrl(String newThumbnailUrl) { // 썸네일 URL을 변경하는 도메인 메서드
        this.thumbnailUrl = newThumbnailUrl; // 전달된 새 URL을 엔티티 필드에 반영
    }
}
