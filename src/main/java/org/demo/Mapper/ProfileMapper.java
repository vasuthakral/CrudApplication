package org.demo.Mapper;

import org.demo.dto.ProfileRequest;
import org.demo.dto.ProfileResponse;
import org.demo.model.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProfileMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Profile toProfile(ProfileRequest profileRequest);

    ProfileResponse toProfileResponse(Profile profile);
}
