package org.mrshoffen.cloudstorage.user.mapper;

import org.mapstruct.Mapper;
import org.mrshoffen.cloudstorage.user.model.dto.UserResponseDto;
import org.mrshoffen.cloudstorage.user.model.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDto(User user);
}
