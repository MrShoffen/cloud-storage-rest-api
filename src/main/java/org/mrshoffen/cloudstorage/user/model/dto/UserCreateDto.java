package org.mrshoffen.cloudstorage.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateDto(
        @Size(min = 5, max = 20, message = "Incorrect name length! Must be between  {min} and {max}")
        @NotBlank(message = "Username can't be empty!")
        @Pattern(regexp = "^[a-zA-Z]+[a-zA-Z_]*[a-zA-Z.]+$", message = "Incorrect symbols in username!")
        String username,

        @Size(min = 5, max = 20, message = "Incorrect password length! Must be between  {min} and {max}")
        @NotBlank(message = "Password can't be empty!")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*(),.?:{}|<>/`~+=-_';]*$", message = "Incorrect symbols in password!")
        String password
) {
}
