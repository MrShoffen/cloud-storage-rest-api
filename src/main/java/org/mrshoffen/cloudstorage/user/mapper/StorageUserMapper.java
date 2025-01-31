package org.mrshoffen.cloudstorage.user.mapper;

import org.mapstruct.Mapper;
import org.mrshoffen.cloudstorage.user.dto.StorageUserResponseDto;
import org.mrshoffen.cloudstorage.user.entity.StorageUser;

@Mapper(componentModel = "spring")
public interface StorageUserMapper {

    StorageUserResponseDto toDto(StorageUser storageUser);
}
