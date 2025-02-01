package org.mrshoffen.cloudstorage.user.model.dto;

import org.mrshoffen.cloudstorage.user.util.validation.ValidPassword;


public record UserPasswordEditDto(@ValidPassword String oldPassword,
                                  @ValidPassword String newPassword) {
}
