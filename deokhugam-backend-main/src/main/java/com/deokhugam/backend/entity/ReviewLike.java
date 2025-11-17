package com.deokhugam.backend.entity;

import com.deokhugam.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "review_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_likes_review_user",
                columnNames = {"review_id", "user_id"}
        )) // 유저-리뷰 간에 좋아요는 하나여야하므로 unique 설정
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class ReviewLike extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // 필수 FK
    @JoinColumn(name = "review_id", nullable = false, foreignKey = @ForeignKey(name = "fk_like_review")) // FK 이름
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review; // 리뷰

    @ManyToOne(optional = false, fetch = FetchType.LAZY) // 필수 FK
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_like_user")) // FK 이름
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

}
