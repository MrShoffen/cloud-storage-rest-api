package org.mrshoffen.cloudstorage.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor()
@NoArgsConstructor
public class FileResponseDto extends FolderFileResponseDto {

    Long size;
}
