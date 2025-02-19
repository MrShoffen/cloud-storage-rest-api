package org.mrshoffen.cloudstorage.storage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StorageObjectResponse implements Serializable {
    private String name;
    private String path;
    private Long size;
    ZonedDateTime lastModified;
    private boolean isFolder;
}
