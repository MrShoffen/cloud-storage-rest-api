package org.mrshoffen.cloudstorage.session.service;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final RedisIndexedSessionRepository sessionRepository;

    public void updateAllUserSessions(String userForUpdate, User updatedUser) {
        Collection<? extends Session> userSessions = sessionRepository.findByPrincipalName(userForUpdate).values();

        for (Session session : userSessions) {
            SecurityContext context = session.getAttribute("SPRING_SECURITY_CONTEXT");
            ((StorageUserDetails) context.getAuthentication().getPrincipal()).setUser(updatedUser);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            RedisIndexedSessionRepository.RedisSession redSession = (RedisIndexedSessionRepository.RedisSession) session;

            sessionRepository.save(redSession);
        }
    }

    public void invalidateAllUserOtherSessions(String currentUser) {

        String currentUserSession = RequestContextHolder.getRequestAttributes().getSessionId();

        sessionRepository
                .findByPrincipalName(currentUser)
                .keySet()
                .stream()
                .filter(session -> !session.equals(currentUserSession))
                .forEach(sessionRepository::deleteById);
    }
}
