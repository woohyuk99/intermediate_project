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
@Table(name = "popular_books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class PopularBook extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pop_book"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Period period;

    @Column(name = "period_date", nullable = false)
    private Instant periodDate;

    @Column(nullable = false)
    private Long rank;

    @Column(nullable = false)
    private Double score;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount;

    @Column(name = "rating", nullable = false)
    private Double rating;

}
