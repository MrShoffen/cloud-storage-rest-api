package org.mrshoffen.cloudstorage.session.events.listener;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.session.service.SessionService;
import org.mrshoffen.cloudstorage.user.events.UserPasswordUpdateEvent;
import org.mrshoffen.cloudstorage.user.events.UserUpdateInfoEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventListener {

    private final SessionService sessionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserInfoUpdate(UserUpdateInfoEvent userUpdateEvent) {

        sessionService.updateAllUserSessions(userUpdateEvent.principalUsername(), userUpdateEvent.updatedUser());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPasswordUpdate(UserPasswordUpdateEvent userPasswordUpdateEvent){
        sessionService.invalidateAllUserOtherSessions(userPasswordUpdateEvent.principalUsername());
    }

}
