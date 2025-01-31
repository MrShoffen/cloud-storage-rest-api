package org.mrshoffen.cloudstorage.user.events;

import org.mrshoffen.cloudstorage.user.entity.User;


public record UserUpdateEvent(
        String principalUsername,
        User updatedUser) {
}
