package org.mrshoffen.cloudstorage.storage.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.dto.response.FileResponseDto;
import org.mrshoffen.cloudstorage.storage.dto.response.FolderFileResponseDto;
import org.mrshoffen.cloudstorage.storage.dto.response.ObjectManageResponse;
import org.mrshoffen.cloudstorage.storage.service.MinioService;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1/files") //todo rename endpoint
@RequiredArgsConstructor
public class TestController {

    @Value("${minio.bucket-name}")
    private String bucket;

    private final MinioClient minioClient;

    private final MinioService minioService;

    @SneakyThrows
    @GetMapping
    public ResponseEntity<List<FolderFileResponseDto>> test(@AuthenticationPrincipal(expression = "getUser") User user,
                                                            @RequestParam(value = "object") String objectName) {
//todo add validation

        List<FolderFileResponseDto> foldersAndFiles;
        if (objectName.isBlank()) {
            foldersAndFiles = minioService.usersRootFolderContent(user.getId());
        } else {
            foldersAndFiles = minioService.userFolderItems(user.getId(), objectName);
        }
        return ResponseEntity.ok(foldersAndFiles);
    }


    @SneakyThrows
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal(expression = "getUser") User user,
                                             @RequestParam(value = "object") String objectPath) {


        List<FolderFileResponseDto> folderFileResponseDtos = minioService.userFolderItems(user.getId(), objectPath);

        Long size = ((FileResponseDto) folderFileResponseDtos.get(0)).getSize();

        Resource resource = minioService.downloadUserItems(user.getId(), objectPath);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectPath + "\"");
        headers.setContentLength(size);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);


    }

    @PostMapping
    public void upload(@RequestPart(required = false, name = "files") List<MultipartFile> files, String path) {


        return;
    }

    @PostMapping("/copy")
    public ResponseEntity<ObjectManageResponse> copyObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                           @RequestBody CopyMoveRequest copyDto) {
        minioService.copyUserItems(user.getId(), copyDto);

        return ResponseEntity
                .created(
                        UriComponentsBuilder
                                .fromPath("/api/v1/folders/{path}")
                                .build(Map.of("path", copyDto.targetPath()))
                )
                .body(
                        ObjectManageResponse.builder()
                                .message("Копирование успешно выполнено")
                                .path(copyDto.targetPath())
                                .build()
                );
    }

    @PutMapping("/move")
    public ResponseEntity<ObjectManageResponse> moveObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                           @RequestBody CopyMoveRequest moveDto) {
        minioService.moveUserItems(user.getId(), moveDto);

        return ResponseEntity
                .ok()
                .body(
                        ObjectManageResponse.builder()
                                .message("Перемещение успешно выполнено")
                                .path(moveDto.targetPath())
                                .build()
                );
    }

    @DeleteMapping
    public ResponseEntity<ObjectManageResponse> deleteObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                             @RequestParam(value = "object") String objectName) {

        minioService.deleteUserItems(user.getId(), objectName);
        return ResponseEntity
                .ok()
                .body(
                        ObjectManageResponse.builder()
                                .message("Удаление успешно выполнено")
                                .build()
                );
    }

}
