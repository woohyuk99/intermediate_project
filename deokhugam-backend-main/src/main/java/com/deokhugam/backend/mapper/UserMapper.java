package com.deokhugam.backend.mapper;

import com.deokhugam.backend.dto.user.UserDto;
import com.deokhugam.backend.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
}
