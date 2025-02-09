package org.mrshoffen.cloudstorage.storage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor()
@NoArgsConstructor
public class FileResponseDto extends FolderFileResponseDto {

    Long size;
    ZonedDateTime lastModified;
}
