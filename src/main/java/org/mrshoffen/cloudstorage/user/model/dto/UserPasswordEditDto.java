package org.mrshoffen.cloudstorage.user.model.dto;

import org.mrshoffen.cloudstorage.user.util.validation.ValidPassword;
import org.mrshoffen.cloudstorage.user.util.validation.ValidUsername;


public record UserPasswordEditDto(@ValidPassword String oldPassword,
                                  @ValidUsername String newPassword) {
}
