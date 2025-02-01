package org.mrshoffen.cloudstorage.user.model.dto;

import org.mrshoffen.cloudstorage.user.util.validation.ValidUsername;

public record UserInfoEditDto(@ValidUsername String newUsername,
                              String newAvatarUrl) {
}
