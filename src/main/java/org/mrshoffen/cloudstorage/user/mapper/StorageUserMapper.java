package org.mrshoffen.cloudstorage.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mrshoffen.cloudstorage.security.dto.StorageUserResponseDto;
import org.mrshoffen.cloudstorage.user.entity.StorageUser;

@Mapper(componentModel = "spring")
public interface StorageUserMapper {

    @Mapping(target = "id",source = "id")
    @Mapping(target = "username",source = "username")
    StorageUserResponseDto toDto(StorageUser storageUser);
}
