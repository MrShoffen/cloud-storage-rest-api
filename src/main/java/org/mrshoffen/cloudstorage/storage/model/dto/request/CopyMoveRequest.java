package org.mrshoffen.cloudstorage.storage.model.dto.request;

public record CopyMoveRequest(
        //todo add validation (both must be either files or folders
        String sourcePath,
        String targetPath
) {
}
