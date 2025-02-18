package org.mrshoffen.cloudstorage.logging.storage;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LoggingStorageServiceAspect {

    @Pointcut("execution(* org.mrshoffen.cloudstorage.storage.service.UserStorageService.*(*,String))")
    public void storageOperationsWithTwoArgs() {
    }

    @AfterReturning(value = "storageOperationsWithTwoArgs()", returning = "resource")
    public void afterReturningTwoArgsMethod(JoinPoint joinPoint, Object resource) {
        Object[] args = joinPoint.getArgs();
        User user = (User) args[0];
        Long userId = user.getId();
        String requestPath = (String) args[1];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : SUCCESS : User[{}] : Request path - {} \n ->Resource: {}", className, method,
                user,
                requestPath == null || requestPath.isBlank() ? "[root folder]" : requestPath,
                resource);
    }

    @AfterThrowing(value = "storageOperationsWithTwoArgs()", throwing = "ex")
    public void afterFailTwoArgsMethod(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        User user = (User) args[0];
        Long userId = user.getId();
        String requestPath = (String) args[1];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : FAILED : User[{}] : Request path - {} : Reason - {}", className, method,
                user,
                requestPath == null || requestPath.isBlank() ? "[root folder]" : requestPath,
                ex.toString());
    }

    @Pointcut("execution(* org.mrshoffen.cloudstorage.storage.service.UserStorageService.*(*, String, String))")
    public void storageOperationsWithCopyDto() {
    }

    @AfterReturning(value = "storageOperationsWithCopyDto()")
    public void afterReturningCopyDtoMethod(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        User user = (User) args[0];
        Long userId = user.getId();
        String source = (String) args[1];
        String target = (String) args[2];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : SUCCESS : User[{}] : From - {} : To - {}", className, method,
                user,
                source,
                target);
    }

    @AfterThrowing(value = "storageOperationsWithCopyDto()", throwing = "ex")
    public void afterFailCopyDtoMethod(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        User user = (User) args[0];
        Long userId = user.getId();
        String source = (String) args[1];
        String target = (String) args[2];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : FAILED : User[{}] : From - {} : To - {} : Reason - {}", className, method,
                user,
                source,
                target,
                ex.toString());
    }

    @Pointcut("execution(* org.mrshoffen.cloudstorage.storage.service.UserStorageService.uploadObjectsToFolder(*, *, String))")
    public void uploadOperation() {
    }

    @AfterReturning(value = "uploadOperation()", returning = "response")
    public void afterReturningUploadMethod(JoinPoint joinPoint, Object response) {
        Object[] args = joinPoint.getArgs();
        User user = (User) args[0];
        Long userId = user.getId();
        List<MultipartFile> files = (List<MultipartFile>) args[1];
        String targetFolder = (String) args[2];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : UPLOADED : User[{}] : Target folder - {} \n -> Input files:\n   {} \n -> Response - {}", className, method,
                user,
                targetFolder,
                files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.joining("\n   ")),
                response
        );

    }

    @AfterThrowing(value = "uploadOperation()", throwing = "ex")
    public void afterFailUploadMethod(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        User user = (User) args[0];
        Long userId = user.getId();
        List<MultipartFile> files = (List<MultipartFile>) args[1];
        String targetFolder = (String) args[2];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : UPLOAD FAILED : User[{}] : Target folder - {} \n -> Input files:\n   {} \n -> Reason - {}", className, method,
                user,
                targetFolder,
                files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.joining("\n   ")),
                ex.toString()
        );

    }

}
