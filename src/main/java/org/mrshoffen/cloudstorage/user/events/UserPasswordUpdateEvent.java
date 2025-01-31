package org.mrshoffen.cloudstorage.user.events;

public record UserPasswordUpdateEvent(
        String principalUsername
) {
}
