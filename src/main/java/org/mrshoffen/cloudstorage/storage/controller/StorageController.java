package org.mrshoffen.cloudstorage.storage.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.model.StorageObject;
import org.mrshoffen.cloudstorage.storage.model.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageObjectResourceDto;
import org.mrshoffen.cloudstorage.storage.model.dto.response.StorageOperationResponse;
import org.mrshoffen.cloudstorage.storage.service.UserStorageService;
import org.mrshoffen.cloudstorage.user.model.entity.User;
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

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/v1/files") //todo rename endpoint
@RequiredArgsConstructor
public class StorageController {

    private final UserStorageService userStorageService;

    @GetMapping
    public ResponseEntity<StorageObject> objectStats(@AuthenticationPrincipal(expression = "getUser") User user,
                                                     @RequestParam(value = "object", required = false) String object) {
        StorageObject stats = userStorageService.getObjectStats(user.getId(), object);
        return ResponseEntity.ok(stats);
    }

    @SneakyThrows
    @GetMapping("/list")
    public ResponseEntity<List<StorageObject>> test(@AuthenticationPrincipal(expression = "getUser") User user,
                                                    @RequestParam(value = "folder") String folder) {
        List<StorageObject> content = userStorageService.listObjectsInFolder(user.getId(), folder);
        return ResponseEntity.ok(content);
    }

    @SneakyThrows
    @GetMapping("/preview")
    public String getObjectPreview(@AuthenticationPrincipal(expression = "getUser") User user,
                                   @RequestParam(value = "object") String objectPath) {

        return userStorageService.getPreviewLink(user.getId(), objectPath);
    }

    @SneakyThrows
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                   @RequestParam(value = "object") String objectPath) {

        StorageObjectResourceDto resource = userStorageService.downloadObject(user.getId(), objectPath);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getNameForSave() + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource.getDownloadResource());
    }

    @PostMapping("/upload")
    public ResponseEntity<List<StorageOperationResponse>> uploadObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                                       @RequestPart(required = false, name = "object") List<MultipartFile> files,
                                                                       @RequestParam(value = "folder", required = false) String folder) {

        List<StorageOperationResponse> response = userStorageService.uploadObjectsToFolder(user.getId(), files, folder);

        return ResponseEntity.status(MULTI_STATUS)
                .body(response);
    }

    @PostMapping("/copy")
    public ResponseEntity<StorageOperationResponse> copyObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                               @RequestBody CopyMoveRequest copyDto) {
        userStorageService.copyObject(user.getId(), copyDto);

        return ResponseEntity
                .created(
                        UriComponentsBuilder
                                .fromPath("/api/v1/folders/{path}")
                                .build(Map.of("path", copyDto.targetPath()))
                )
                .body(
                        StorageOperationResponse.builder()
                                .status(CREATED.value())
                                .title(CREATED.getReasonPhrase())
                                .detail("Копирование успешно выполнено")
                                .path(copyDto.targetPath())
                                .build()
                );
    }

    @PutMapping("/move")
    public ResponseEntity<StorageOperationResponse> moveObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                               @RequestBody CopyMoveRequest moveDto) {
        userStorageService.moveObject(user.getId(), moveDto);
        return ResponseEntity
                .ok()
                .body(
                        StorageOperationResponse.builder()
                                .status(OK.value())
                                .title(OK.getReasonPhrase())
                                .detail("Перемещение успешно выполнено")
                                .path(moveDto.targetPath())
                                .build()
                );
    }

    @DeleteMapping
    public ResponseEntity<StorageOperationResponse> deleteObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                                 @RequestParam(value = "object") String objectName) {

        userStorageService.deleteObject(user.getId(), objectName);
        return ResponseEntity
                .ok()
                .body(
                        StorageOperationResponse.builder()
                                .status(NO_CONTENT.value())
                                .title(NO_CONTENT.getReasonPhrase())
                                .detail("Удаление успешно выполнено")
                                .build()
                );
    }

}
