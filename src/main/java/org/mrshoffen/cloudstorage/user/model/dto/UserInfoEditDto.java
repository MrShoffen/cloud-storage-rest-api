package org.mrshoffen.cloudstorage.user.model.dto;

import org.mrshoffen.cloudstorage.user.validation.ValidUsername;

public record UserInfoEditDto(@ValidUsername String newUsername,
                              String newAvatarUrl) {
}
