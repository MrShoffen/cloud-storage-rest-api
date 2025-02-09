package org.mrshoffen.cloudstorage.storage.dto.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class FolderFileResponseDto {

    protected String path;
    protected String name;
    protected boolean isFolder;
}
