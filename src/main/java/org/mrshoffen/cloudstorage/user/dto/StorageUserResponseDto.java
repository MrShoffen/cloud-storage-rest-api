package org.mrshoffen.cloudstorage.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorageUserResponseDto {

    private Long id;

    private String username;

    private String avatarUrl;

}
