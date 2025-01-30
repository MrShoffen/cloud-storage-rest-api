package org.mrshoffen.cloudstorage.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageUserResponseDto {

    private Long id;

    private String username;
}
