package org.mrshoffen.cloudstorage.storage.controller;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.DownloadStorageObjectDto;
import org.mrshoffen.cloudstorage.storage.dto.StorageObject;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.dto.response.ObjectManageResponse;
import org.mrshoffen.cloudstorage.storage.service.UserStorageService;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files") //todo rename endpoint
@RequiredArgsConstructor
public class TestController {

    @Value("${minio.bucket-name}")
    private String bucket;

    private final MinioClient minioClient;

    private final UserStorageService userStorageService;

    @SneakyThrows
    @GetMapping
    public ResponseEntity<List<StorageObject>> test(@AuthenticationPrincipal(expression = "getUser") User user,
                                                    @RequestParam(value = "object") String objectName) {
//todo add validation

        List<StorageObject> foldersAndFiles;
        if (objectName.isBlank()) {
            foldersAndFiles = userStorageService.rootStorageObjects(user.getId());
        } else {
            foldersAndFiles = userStorageService.storageObjectsFromPath(user.getId(), objectName);
        }
        return ResponseEntity.ok(foldersAndFiles);
    }


    @SneakyThrows
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal(expression = "getUser") User user,
                                             @RequestParam(value = "object") String objectPath) {

        DownloadStorageObjectDto resource = userStorageService.downloadUserItems(user.getId(), objectPath);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getNameForSave() + "\"");
//        headers.setContentLength(resource.getSize());

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource.getDownloadResource());
    }

    @PostMapping
    public void upload(@RequestPart(required = false, name = "files") List<MultipartFile> files, String path) {


        return;
    }

    @PostMapping("/copy")
    public ResponseEntity<ObjectManageResponse> copyObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                           @RequestBody CopyMoveRequest copyDto) {
        userStorageService.copyUserItems(user.getId(), copyDto);

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
        userStorageService.moveUserItems(user.getId(), moveDto);

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

        userStorageService.deleteUserItems(user.getId(), objectName);
        return ResponseEntity
                .ok()
                .body(
                        ObjectManageResponse.builder()
                                .message("Удаление успешно выполнено")
                                .build()
                );
    }

}
