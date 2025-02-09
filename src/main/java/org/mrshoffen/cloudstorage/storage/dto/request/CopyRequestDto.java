package org.mrshoffen.cloudstorage.storage.dto.request;

public record CopyRequestDto(
        //todo add validation
        String sourcePath,
        String targetPath

) {
}
