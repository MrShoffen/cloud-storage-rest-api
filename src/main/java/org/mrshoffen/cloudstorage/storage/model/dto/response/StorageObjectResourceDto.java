package org.mrshoffen.cloudstorage.storage.model.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class StorageObjectResourceDto {
    private Resource downloadResource;
    private String nameForSave;
}
