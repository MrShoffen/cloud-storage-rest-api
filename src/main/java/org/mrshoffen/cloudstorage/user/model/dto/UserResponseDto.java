package org.mrshoffen.cloudstorage.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;

    private String username;

    private String avatarUrl;

}
