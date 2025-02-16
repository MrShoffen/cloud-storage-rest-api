package org.mrshoffen.cloudstorage.storage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StorageObjectResponse {
    private String name;
    private String path;
    private Long size;
    ZonedDateTime lastModified;
    private boolean isFolder;
}
