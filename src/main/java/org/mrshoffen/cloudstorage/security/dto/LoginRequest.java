package org.mrshoffen.cloudstorage.security.dto;

import org.mrshoffen.cloudstorage.user.util.validation.ValidPassword;
import org.mrshoffen.cloudstorage.user.util.validation.ValidUsername;


public record LoginRequest(@ValidUsername String username,
                           @ValidPassword String password) {
}