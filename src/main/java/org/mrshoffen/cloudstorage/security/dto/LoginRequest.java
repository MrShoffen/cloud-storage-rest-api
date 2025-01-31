package org.mrshoffen.cloudstorage.security.dto;

import org.mrshoffen.cloudstorage.user.validation.ValidPassword;
import org.mrshoffen.cloudstorage.user.validation.ValidUsername;


public record LoginRequest(@ValidUsername String username,
                           @ValidPassword String password) {
}