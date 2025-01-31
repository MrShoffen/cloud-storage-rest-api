package org.mrshoffen.cloudstorage.user.events.publisher;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.mrshoffen.cloudstorage.user.events.UserPasswordUpdateEvent;
import org.mrshoffen.cloudstorage.user.events.UserUpdateInfoEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishUserInfoUpdateEvent(String principalUsername, User updatedUser) {
        UserUpdateInfoEvent userUpdateEvent = new UserUpdateInfoEvent(principalUsername, updatedUser);
        eventPublisher.publishEvent(userUpdateEvent);
    }

    public void publishUserPasswordUpdateEvent(String principalUsername) {
        UserPasswordUpdateEvent userPasswordUpdateEvent = new UserPasswordUpdateEvent(principalUsername);
        eventPublisher.publishEvent(userPasswordUpdateEvent);
    }
}
