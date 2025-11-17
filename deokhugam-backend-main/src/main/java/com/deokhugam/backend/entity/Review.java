package com.deokhugam.backend.entity;

import com.deokhugam.backend.dto.review.ReviewUpdateRequest;
import com.deokhugam.backend.entity.base.BaseSoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Objects;

@Getter
@Entity
@Table(name = "reviews") // unique 제약조건 삭제
// sql파일 추가하게 되면 DB 차원에서 조건부 unique 제약조건 걸 수 있음
// 논리 삭제 후 리뷰 재등록이 제약때문에 불가능
/*
CREATE UNIQUE INDEX IF NOT EXISTS uk_reviews_user_book_active
ON reviews(user_id, book_id)
WHERE deleted_at IS NULL;
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Review extends BaseSoftDeletableEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_user"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_review_book"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    // 쿼리 오류나서 @Lob 제거
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "comment_count")
    private Integer commentCount = 0;

    public void update(ReviewUpdateRequest request) {
        if (!Objects.equals(request.content(), this.content)) {
            this.content = request.content();
        }
        if (!Objects.equals(request.rating(), this.rating)) {
            this.rating = request.rating();
        }
    }

    // 좋아요 수 증가
    public void incrementLikeCount() {
        this.likeCount++;
    }

    // 좋아요 수 감소
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
