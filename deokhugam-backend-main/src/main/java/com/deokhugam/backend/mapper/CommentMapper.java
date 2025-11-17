package com.deokhugam.backend.mapper;

import com.deokhugam.backend.dto.comment.CommentCreateRequest;
import com.deokhugam.backend.dto.comment.CommentDto;
import com.deokhugam.backend.entity.Comment;
import com.deokhugam.backend.entity.Review;
import com.deokhugam.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN) // 스프링 빈 등록 + 누락 매핑은 화면 표시만
public interface CommentMapper {

    // 요청 → 엔티티 (연관 주입 + content trim)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "review", source = "review")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "content", expression = "java(request.content() == null ? \"\" : request.content().trim())")
    Comment toEntity(CommentCreateRequest request, User user, Review review);

    @Mapping(target = "reviewId", source = "review.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userNickname", source = "user.nickname")
    CommentDto toDto(Comment comment);
}
