package org.mrshoffen.cloudstorage.storage.controller;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.request.CopyMoveRequest;
import org.mrshoffen.cloudstorage.storage.dto.response.FolderFileResponseDto;
import org.mrshoffen.cloudstorage.storage.dto.response.ObjectManageResponse;
import org.mrshoffen.cloudstorage.storage.service.MinioService;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/folders") //todo rename endpoint
@RequiredArgsConstructor
public class TestController {

    @Value("${minio.bucket-name}")
    private String bucket;

    private final MinioClient minioClient;

    private final MinioService minioService;

    @SneakyThrows
    @GetMapping
    public ResponseEntity<List<FolderFileResponseDto>> test(@AuthenticationPrincipal(expression = "getUser") User user,
                                                            @RequestParam(name = "folder-name", required = false) String folderName) {
        List<FolderFileResponseDto> foldersAndFiles;
        if (folderName == null) {
            foldersAndFiles = minioService.usersRootFolderContent(user.getId());
        } else {
            foldersAndFiles = minioService.userFolderItems(user.getId(), folderName);
        }
        return ResponseEntity.ok(foldersAndFiles);
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
                                .message("Copied successfully")
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
                                .message("Moved successfully")
                                .path(moveDto.targetPath())
                                .build()
                );
    }

    @DeleteMapping
    public ResponseEntity<ObjectManageResponse> deleteObject(@AuthenticationPrincipal(expression = "getUser") User user,
                                                             @RequestParam(name = "file-name") String deletingFile) {

        minioService.deleteUserItems(user.getId(), deletingFile);
        return ResponseEntity
                .ok()
                .body(
                        ObjectManageResponse.builder()
                                .message("Deleted successfully")
                                .build()
                );
    }

}
