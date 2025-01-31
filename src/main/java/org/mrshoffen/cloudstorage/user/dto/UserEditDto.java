package org.mrshoffen.cloudstorage.user.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserEditDto(

        @Size(min = 5, max = 20, message = "Incorrect name length! Must be between  {min} and {max}")
        @NotNull(message = "Name can't be null!")
        @Pattern(regexp = "^[a-zA-Z]+[a-zA-Z_]*[a-zA-Z.]+$", message = "Incorrect symbols in username!")
        String newUsername,

        String newAvatarUrl
) {
}
