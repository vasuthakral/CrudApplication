package org.demo.Mapper;

import org.demo.dto.PostRequest;
import org.demo.dto.PostResponse;
import org.demo.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = TagMapper.class)
public interface PostMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tags", ignore = true)
    Post toPost(PostRequest postRequest);

    PostResponse toPostResponse(Post post);

}

