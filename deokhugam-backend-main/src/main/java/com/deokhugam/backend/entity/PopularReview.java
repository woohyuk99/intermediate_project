package com.deokhugam.backend.entity;

import com.deokhugam.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "popular_reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class PopularReview extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pop_review"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Period period;

    @Column(name = "period_date", nullable = false)
    private Instant periodDate;

    @Column(nullable = false)
    private Long rank;

    @Column(nullable = false)
    private Double score;

    @Column(name = "like_count", nullable = false)
    private Long likeCount;

    @Column(name = "comment_count", nullable = false)
    private Long commentCount;

}
