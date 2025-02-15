package org.mrshoffen.cloudstorage.logging.storage;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.mrshoffen.cloudstorage.storage.model.dto.request.CopyMoveRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LoggingStorageServiceAspect {

    @Pointcut("execution(* org.mrshoffen.cloudstorage.storage.service.UserStorageService.*(Long,String))")
    public void storageOperationsWithTwoArgs() {
    }

    @AfterReturning(value = "storageOperationsWithTwoArgs()", returning = "resource")
    public void afterReturningTwoArgsMethod(JoinPoint joinPoint, Object resource) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        String requestPath = (String) args[1];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : SUCCESS : User[{}] : Request path - {} \n ->Resource: {}", className, method,
                userId,
                requestPath == null || requestPath.isBlank() ? "[root folder]" : requestPath,
                resource);
    }

    @AfterThrowing(value = "storageOperationsWithTwoArgs()", throwing = "ex")
    public void afterFailTwoArgsMethod(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        String requestPath = (String) args[1];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : FAILED : User[{}] : Request path - {} : Reason - {}", className, method,
                userId,
                requestPath == null || requestPath.isBlank() ? "[root folder]" : requestPath,
                ex.toString());
    }

    @Pointcut("execution(* org.mrshoffen.cloudstorage.storage.service.UserStorageService.*(Long,org.mrshoffen.cloudstorage.storage.model.dto.request.CopyMoveRequest))")
    public void storageOperationsWithCopyDto() {
    }

    @AfterReturning(value = "storageOperationsWithCopyDto()")
    public void afterReturningCopyDtoMethod(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        CopyMoveRequest dto = (CopyMoveRequest) args[1];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : SUCCESS : User[{}] : From - {} : To - {}", className, method,
                userId,
                dto.sourcePath(),
                dto.targetPath());
    }

    @AfterThrowing(value = "storageOperationsWithCopyDto()", throwing = "ex")
    public void afterFailCopyDtoMethod(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        CopyMoveRequest dto = (CopyMoveRequest) args[1];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : FAILED : User[{}] : From - {} : To - {} : Reason - {}", className, method,
                userId,
                dto.sourcePath(),
                dto.targetPath(),
                ex.toString());
    }

    @Pointcut("execution(* org.mrshoffen.cloudstorage.storage.service.UserStorageService.uploadObjectsToFolder(Long, *, *))")
    public void uploadOperation() {
    }

    @AfterReturning(value = "uploadOperation()", returning = "response")
    public void afterReturningUploadMethod(JoinPoint joinPoint, Object response) {
        Object[] args = joinPoint.getArgs();
        Long userId = (Long) args[0];
        List<MultipartFile> files = (List<MultipartFile>) args[1];
        String targetFolder = (String) args[2];

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String method = joinPoint.getSignature().getName();

        log.info("{} : {} : UPLOADED : User[{}] : Target folder - {} \n -> Input files:\n   {} \n -> Response - {}", className, method,
                userId,
                targetFolder,
                files.stream().map(MultipartFile::getOriginalFilename).collect(Collectors.joining("\n   ")),
                response
        );

    }

}
