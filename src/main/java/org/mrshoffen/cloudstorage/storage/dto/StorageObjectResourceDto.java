package org.mrshoffen.cloudstorage.storage.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class StorageObjectResourceDto {
    private Resource downloadResource;
    private String nameForSave;
}
