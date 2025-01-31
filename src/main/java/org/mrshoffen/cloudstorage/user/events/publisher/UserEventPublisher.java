package org.mrshoffen.cloudstorage.user.events.publisher;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.user.entity.User;
import org.mrshoffen.cloudstorage.user.events.UserUpdateEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishUserUpdateEvent(String principalUsername, User updatedUser) {
        UserUpdateEvent userUpdateEvent = new UserUpdateEvent(principalUsername, updatedUser);
        eventPublisher.publishEvent(userUpdateEvent);
    }
}
