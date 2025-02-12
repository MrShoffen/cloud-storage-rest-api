package org.mrshoffen.cloudstorage.storage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StorageObjectOperationResponse {
    private String path;
    private String message;
}
