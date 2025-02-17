package org.mrshoffen.cloudstorage.logging.session;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.mrshoffen.cloudstorage.user.events.UserPasswordUpdateEvent;
import org.mrshoffen.cloudstorage.user.events.UserUpdateInfoEvent;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingSessionServiceAspect {


    @Pointcut("execution(* org.mrshoffen.cloudstorage.session.events.listener.SessionEventListener.*(..))")
    public void eventHandlerPointcut() {
    }

    @Before("eventHandlerPointcut()")
    public void beforeUserEventHandled(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("{} -> received {} in {} : {}", className, args[0].getClass().getSimpleName(), method, args[0]);
        if (method.equals("handleUserInfoUpdate")) {
            log.info("-- [{}] update all sessions with new context information --", ((UserUpdateInfoEvent) args[0]).updatedUser().getUsername());
        }
        if (method.equals("handleUserPasswordUpdate")) {
            log.info("-- [{}] invalidate all other sessions except current one --", ((UserPasswordUpdateEvent) args[0]).principalUsername());
        }
    }

    @Pointcut("execution(* org.mrshoffen.cloudstorage.session.service.SessionService.updateAllUserSessions(String, *))")
    public void sessionsUpdatePointcut() {
    }

    @Before(value = "sessionsUpdatePointcut()")
    public void beforeSessionsUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("{} : {} for {} with new context: {}", className, method, args[0], args[1]);


    }

    @AfterReturning(value = "sessionsUpdatePointcut()")
    public void afterSessionsUpdate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("{} : SUCCESS {} for {} with new context: {}", className, method, args[0], args[1]);

    }


    @Pointcut("execution(* org.mrshoffen.cloudstorage.session.service.SessionService.invalidateAllUserOtherSessions(..))")
    public void sessionInvalidatePointcut() {
    }

    @Before(value = "sessionInvalidatePointcut()")
    public void beforeSessionInvalidate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("{} : {} for {} ", className, method, args[0]);

    }

    @AfterReturning(value = "sessionInvalidatePointcut()")
    public void afterSessionInvalidate(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("{} : SUCCESS {} for {} ", className, method, args[0]);

    }

    @Pointcut(value = "execution(* org.springframework.session.data.redis.RedisIndexedSessionRepository.deleteById(String)))")
    public void sessionDeletePointcut() {
    }

    @AfterReturning("sessionDeletePointcut()")
    public void afterSessionDeleted(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("  {} : {} - {} ", className, method, args[0]);
    }

}
