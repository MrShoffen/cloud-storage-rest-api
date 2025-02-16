package org.mrshoffen.cloudstorage.storage.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.model.StorageObjectStats;
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
@RequestMapping("/api/v1/resources") //todo rename endpoint
@RequiredArgsConstructor
public class StorageController {

    private final UserStorageService userStorageService;

    @GetMapping
    public ResponseEntity<StorageObjectStats> getObjectStats(@AuthenticationPrincipal(expression = "getUser") User user,
                                                             @RequestParam(value = "path") String object) {
        StorageObjectStats stats = userStorageService.getObjectStats(user.getId(), object);
        return ResponseEntity.ok(stats);
    }

    @PutMapping
    public ResponseEntity<StorageOperationResponse> createFolder(@AuthenticationPrincipal(expression = "getUser") User user,
                                                                 @RequestParam(value = "path") String folderPath) {

        userStorageService.createFolder(user.getId(), folderPath);

        return ResponseEntity
                .created(
                        UriComponentsBuilder
                                .fromPath("{path}")
                                .build(Map.of("path", folderPath))
                )
                .body(
                        StorageOperationResponse.builder()
                                .status(CREATED.value())
                                .title(CREATED.getReasonPhrase())
                                .detail("Папка успешно создана")
                                .path(folderPath)
                                .build()
                );
    }

    @SneakyThrows
    @GetMapping("/files")
    public ResponseEntity<List<StorageObjectStats>> getObjectsInFolder(@AuthenticationPrincipal(expression = "getUser") User user,
                                                                       @RequestParam(value = "path") String folderPath) {
        List<StorageObjectStats> content = userStorageService.listObjectsInFolder(user.getId(), folderPath);
        return ResponseEntity.ok(content);
    }

    @SneakyThrows
    @GetMapping("/preview")
    public String getObjectPreview(@AuthenticationPrincipal(expression = "getUser") User user,
                                   @RequestParam(value = "path") String objectPath) {

        return userStorageService.getPreviewLink(user.getId(), objectPath);
    }

    @SneakyThrows
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                   @RequestParam(value = "path") String objectPath) {

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
                                                                       @RequestPart(name = "object") List<MultipartFile> files,
                                                                       @RequestParam(value = "path") String folder) {
        List<StorageOperationResponse> response = userStorageService.uploadObjectsToFolder(user.getId(), files, folder);
        return ResponseEntity.status(MULTI_STATUS)
                .body(response);
    }

    @PostMapping("/copy")
    public ResponseEntity<StorageOperationResponse> copyObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                               @RequestParam(value = "from") String source,
                                                               @RequestParam(value = "path") String target) {
        userStorageService.copyObject(user.getId(), source, target);

        return ResponseEntity
                .created(
                        UriComponentsBuilder
                                .fromPath("{path}")
                                .build(Map.of("path", target))
                )
                .body(
                        StorageOperationResponse.builder()
                                .status(CREATED.value())
                                .title(CREATED.getReasonPhrase())
                                .detail("Копирование успешно выполнено")
                                .path(target)
                                .build()
                );
    }

    @PutMapping("/move")
    public ResponseEntity<StorageOperationResponse> moveObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                               @RequestParam(value = "from") String source,
                                                               @RequestParam(value = "path") String target) {
        userStorageService.moveObject(user.getId(), source, target);
        return ResponseEntity
                .created(
                        UriComponentsBuilder
                                .fromPath("{path}")
                                .build(Map.of("path", target))
                )
                .body(
                        StorageOperationResponse.builder()
                                .status(OK.value())
                                .title(OK.getReasonPhrase())
                                .detail("Перемещение успешно выполнено")
                                .path(target)
                                .build()
                );
    }

    @DeleteMapping
    public ResponseEntity<StorageOperationResponse> deleteObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                                 @RequestParam(value = "path") String objectPath) {

        userStorageService.deleteObject(user.getId(), objectPath);
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
