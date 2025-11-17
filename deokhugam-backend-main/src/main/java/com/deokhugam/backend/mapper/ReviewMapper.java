package com.deokhugam.backend.mapper;

import com.deokhugam.backend.dto.review.ReviewCreateRequest;
import com.deokhugam.backend.dto.review.ReviewDto;
import com.deokhugam.backend.entity.Book;
import com.deokhugam.backend.entity.Review;
import com.deokhugam.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "book", source = "book")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "content", source = "req.content")
    @Mapping(target = "rating", source = "req.rating")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "likeCount", constant = "0")
    @Mapping(target = "commentCount", constant = "0")
    Review toReview(ReviewCreateRequest req, Book book, User user);

    @Mapping(target = "bookId", source = "review.book.id")
    @Mapping(target = "bookTitle", source = "review.book.title")
    @Mapping(target = "bookThumbnailUrl", source = "review.book.thumbnailUrl")
    @Mapping(target = "userId", source = "review.user.id")
    @Mapping(target = "userNickname", source = "review.user.nickname")
    @Mapping(target = "likedByMe", source = "likedByMe")
    ReviewDto toReviewDto(Review review, boolean likedByMe);

}
