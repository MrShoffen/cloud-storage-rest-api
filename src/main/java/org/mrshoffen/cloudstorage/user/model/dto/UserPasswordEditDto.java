package org.mrshoffen.cloudstorage.user.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//todo separate validation annotation
public record UserPasswordEditDto(

        @Size(min = 5, max = 20, message = "Incorrect password length! Must be between  {min} and {max}")
        @NotNull(message = "Password can't be null!")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*(),.?:{}|<>/`~+=-_';]*$", message = "Incorrect symbols in password!")
        String oldPassword,

        @Size(min = 5, max = 20, message = "Incorrect password length! Must be between  {min} and {max}")
        @NotNull(message = "Password can't be null!")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*(),.?:{}|<>/`~+=-_';]*$", message = "Incorrect symbols in password!")
        String newPassword
) {
}
