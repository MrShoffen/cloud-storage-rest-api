package org.mrshoffen.cloudstorage.user.model.dto;

import jakarta.validation.constraints.NotNull;
import org.mrshoffen.cloudstorage.user.model.entity.StoragePlan;
import org.mrshoffen.cloudstorage.user.util.validation.ValidPassword;
import org.mrshoffen.cloudstorage.user.util.validation.ValidUsername;

public record UserCreateDto(
        @ValidUsername
        String username,

        @ValidPassword
        String password,

        String avatarUrl,

        @NotNull(message = "Необходимо указать план")
        StoragePlan storagePlan) {
}
