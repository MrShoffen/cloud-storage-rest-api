package org.mrshoffen.cloudstorage.session.events.listener;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.session.service.SessionService;
import org.mrshoffen.cloudstorage.user.events.UserUpdateEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EventListener {

    private final SessionService sessionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateAllUserSessions(UserUpdateEvent userUpdateEvent) {

        sessionService.updateAllUserSessions(userUpdateEvent.principalUsername(), userUpdateEvent.updatedUser());
    }
}
