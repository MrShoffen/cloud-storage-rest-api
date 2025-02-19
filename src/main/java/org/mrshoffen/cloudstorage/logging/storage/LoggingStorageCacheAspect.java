package org.mrshoffen.cloudstorage.logging.storage;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResponse;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Slf4j
@Component
public class LoggingStorageCacheAspect {

    @Pointcut("execution(* org.mrshoffen.cloudstorage.storage.minio.MinioCacheService.getFolderContent(String))")
    public void getCachedFolderContent() {
    }


    @AfterReturning(value = "getCachedFolderContent()", returning = "resource")
    public void afterReturningTwoArgsMethod(JoinPoint joinPoint, List<StorageObjectResponse> resource) {
        Object[] args = joinPoint.getArgs();
        String path = (String) args[0];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        if (!resource.isEmpty()) {
            log.info("{} : {} : EXTRACTED FROM CACHE : Path - {} : Value - {}", className, method,
                    path,
                    resource);
        }
    }
}
