package org.mrshoffen.cloudstorage.storage.dto;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.time.ZonedDateTime;

@Data
@Builder
public class StorageObjectDto {

    private String name;
    private InputStream inputStream;
    private Long size;
    ZonedDateTime lastModified;

}
