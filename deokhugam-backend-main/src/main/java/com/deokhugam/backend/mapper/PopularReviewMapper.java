package com.deokhugam.backend.mapper;

import com.deokhugam.backend.dto.dashboard.PopularReviewDto;
import com.deokhugam.backend.entity.PopularReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PopularReviewMapper {

    @Mapping(target = "reviewId", source = "popularReview.review.id")
    @Mapping(target = "bookId", source = "popularReview.review.book.id")
    @Mapping(target = "bookTitle", source = "popularReview.review.book.title")
    @Mapping(target = "bookThumbnailUrl", source = "popularReview.review.book.thumbnailUrl")
    @Mapping(target = "userId", source = "popularReview.review.user.id")
    @Mapping(target = "userNickname", source = "popularReview.review.user.nickname")
    @Mapping(target = "reviewContent", source = "popularReview.review.content")
    @Mapping(target = "reviewRating", source = "popularReview.review.rating")
    @Mapping(target = "period", expression = "java(popularReview.getPeriod().name())")
    @Mapping(target = "createdAt", source = "popularReview.review.createdAt")
    PopularReviewDto toPopularReviewDto(PopularReview popularReview, boolean likedByMe);

}
