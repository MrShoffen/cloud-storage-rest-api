package org.mrshoffen.cloudstorage.logging.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LoggingSecurityAspect {

    @Pointcut("execution(* org.mrshoffen.cloudstorage.security.service.StorageUserDetailsService.loadUserByUsername(String)) && args(username)")
    public void loadingUserFromRepoPointcut(String username) {
    }


    @AfterReturning(value = "loadingUserFromRepoPointcut(username)", returning = "user")
    public void afterLoadingUser(JoinPoint joinPoint, String username, UserDetails user) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("{} : {} :username[{}]: SUCCESS : User[{}] ", className, method, username, user);
    }

    @AfterThrowing(value = "loadingUserFromRepoPointcut(username)", throwing = "ex")
    public void afterLoadingUserFail(JoinPoint joinPoint, String username, Exception ex) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.warn("{} : {} : username[{}] : FAILED : Reason - {}", className, method, username, ex.toString());
    }

    @Pointcut("execution(* org.mrshoffen.cloudstorage.security.service.SecurityContextService.saveAuthToContext(*)) && args(auth)")
    public void saveAuthPointcut(Authentication auth) {
    }

    @AfterReturning("saveAuthPointcut(auth)")
    public void afterSuccessSaveContext(JoinPoint joinPoint, Authentication auth) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("{} : {} : SUCCESS : auth[{}]: ", className, method, auth);
    }


}
