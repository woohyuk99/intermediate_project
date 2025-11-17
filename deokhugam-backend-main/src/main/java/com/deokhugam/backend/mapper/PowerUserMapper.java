package com.deokhugam.backend.mapper;

import com.deokhugam.backend.dto.dashboard.PowerUserDto;
import com.deokhugam.backend.entity.PowerUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PowerUserMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "nickname", source = "user.nickname")
    PowerUserDto toDto(PowerUser powerUser);
}
