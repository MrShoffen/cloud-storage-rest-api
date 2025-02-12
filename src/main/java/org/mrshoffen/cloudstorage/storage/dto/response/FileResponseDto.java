package org.mrshoffen.cloudstorage.storage.dto.response;

import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FileResponseDto extends FolderFileResponseDto {

    Long size;
    ZonedDateTime lastModified;
}
