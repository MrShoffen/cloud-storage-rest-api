package org.mrshoffen.cloudstorage.logging.user;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.mrshoffen.cloudstorage.user.events.UserPasswordUpdateEvent;
import org.mrshoffen.cloudstorage.user.events.UserUpdateInfoEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingUserServiceAspect {


    @Pointcut("execution(* org.mrshoffen.cloudstorage.user.service.UserService.*(Long,*)) || execution(* org.mrshoffen.cloudstorage.user.service.UserService.create(*)))")
    public void createUpdatePointcut() {
    }

    @AfterReturning(value = "createUpdatePointcut()", returning = "createdUser")
    public void afterReturning(JoinPoint joinPoint, Object createdUser) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        if (args.length == 2) {
            log.info("{} : {} : SUCCESS : User[{}] : Updated data - {}", className, method, args[0], args[1]);
        } else {
            log.info("{} : {} : SUCCESS : User - {}", className, method, createdUser);
        }
    }

    @AfterThrowing(value = "createUpdatePointcut()", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        if (args.length == 2) {
            log.warn("{} : {} : FAILED : User[{}] : Reason - {}", className, method, args[0], ex.toString());
        } else {
            log.warn("{} : {} : FAILED : Reason - {}", className, method, ex.toString());

        }
    }

    //events

    @Pointcut("execution(* org.mrshoffen.cloudstorage.user.events.publisher.UserEventPublisher.*(..))")
    public void eventPublishingPointcut() {
    }

    @AfterReturning("eventPublishingPointcut()")
    public void afterUserEventPublished(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();
        log.info("{} -> {} :  {}", className, method,  Arrays.toString(args));


    }

}
