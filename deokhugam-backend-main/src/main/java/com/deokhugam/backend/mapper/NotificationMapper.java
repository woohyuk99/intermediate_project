package com.deokhugam.backend.mapper;

import com.deokhugam.backend.dto.notification.NotificationDto;
import com.deokhugam.backend.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

  //TODO reviewTitle 변경해야함
  @Mapping(target = "reviewTitle",  ignore = true)
  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "reviewId", source = "review.id")
  NotificationDto toDto(Notification entity);
}
