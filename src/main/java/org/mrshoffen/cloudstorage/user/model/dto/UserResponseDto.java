package org.mrshoffen.cloudstorage.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mrshoffen.cloudstorage.user.model.entity.StoragePlan;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;

    private String username;

    private String avatarUrl;

    private StoragePlan storagePlan;


}
