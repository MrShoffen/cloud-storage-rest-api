package org.mrshoffen.cloudstorage.user.model.dto;

import org.mrshoffen.cloudstorage.user.validation.ValidPassword;
import org.mrshoffen.cloudstorage.user.validation.ValidUsername;


public record UserPasswordEditDto(@ValidPassword String oldPassword,
                                  @ValidUsername String newPassword) {
}
