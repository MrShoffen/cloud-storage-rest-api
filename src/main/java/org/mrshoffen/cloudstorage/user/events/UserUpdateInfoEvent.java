package org.mrshoffen.cloudstorage.user.events;

import org.mrshoffen.cloudstorage.user.model.entity.User;


public record UserUpdateInfoEvent(
        String principalUsername,
        User updatedUser) {
}
