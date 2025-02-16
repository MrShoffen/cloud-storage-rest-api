package org.mrshoffen.cloudstorage.storage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class StorageOperationResponse {
    private String title;
    private int status;
    private String detail;
    private String path;
}
