package com.deokhugam.backend.mapper;

import com.deokhugam.backend.dto.dashboard.PopularBookDto;
import com.deokhugam.backend.entity.PopularBook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PopularBookMapper {
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "title", source = "book.title")
    @Mapping(target = "author", source = "book.author")
    @Mapping(target = "thumbnailUrl",source ="book.thumbnailUrl")
    PopularBookDto toDto(PopularBook popularBook);
}
