package org.demo.Mapper;

import org.demo.dto.TagRequest;
import org.demo.dto.TagResponse;
import org.demo.model.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "post", ignore = true)
    Tag toTag(TagRequest tagRequest);


    // TagResponse do not have post{they are ignored from source}
    TagResponse toTagResponse(Tag tag);
}
