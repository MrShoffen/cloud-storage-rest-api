package org.mrshoffen.cloudstorage.storage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ObjectManageResponse {
    private String path;
    private String message;
}
