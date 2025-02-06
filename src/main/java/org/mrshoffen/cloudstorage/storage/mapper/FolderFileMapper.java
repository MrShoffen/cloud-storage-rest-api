package org.mrshoffen.cloudstorage.storage.mapper;

import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mrshoffen.cloudstorage.storage.dto.FileResponseDto;
import org.mrshoffen.cloudstorage.storage.dto.FolderFileResponseDto;
import org.mrshoffen.cloudstorage.storage.dto.FolderResponseDto;
import org.springframework.util.StringUtils;

@Mapper(componentModel = "spring")
public interface FolderFileMapper {

    default FolderFileResponseDto toDto(Item item) {
        String absolutePath = item.objectName();

        int lastSlashIndex = absolutePath.lastIndexOf('/', absolutePath.length() - 2);
        String simpleName = absolutePath.substring(lastSlashIndex + 1);

        int firstSlashIndex = absolutePath.indexOf('/');
        String relativePath = absolutePath.substring(firstSlashIndex + 1);

        if (item.isDir()) {
            return toFolderFileResponseDto(item, simpleName, relativePath);
        } else {
            return toFileResponseDto(item, simpleName, relativePath);
        }
    }

    @Mapping(target = "size", expression = "java(item.size())")
    @Mapping(target = "folder", source = "item.dir")
    @Mapping(target = "path", source = "relativePath")
    @Mapping(target = "name", source = "simpleName")
    FileResponseDto toFileResponseDto(Item item, String simpleName, String relativePath);

    @Mapping(target = "folder", source = "item.dir")
    @Mapping(target = "path", source = "relativePath")
    @Mapping(target = "name", source = "simpleName")
    FolderResponseDto toFolderFileResponseDto(Item item, String simpleName, String relativePath);

}
