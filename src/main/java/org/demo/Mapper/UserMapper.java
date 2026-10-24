package org.demo.Mapper;

import org.demo.dto.UserRequest;
import org.demo.dto.UserResponse;
import org.demo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = PostMapper.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "posts", ignore = true)
    User toUser(UserRequest userRequest);

    UserResponse toUserResponse(User user);
}


