package org.mrshoffen.cloudstorage.storage.controller;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.mrshoffen.cloudstorage.storage.dto.FolderFileResponseDto;
import org.mrshoffen.cloudstorage.storage.dto.FolderResponseDto;
import org.mrshoffen.cloudstorage.storage.service.MinioService;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test")
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
            foldersAndFiles = minioService.usersFolderContent(user.getId(), folderName);
        }
        return ResponseEntity.ok(foldersAndFiles);
    }

    @PostMapping
    public void upload(@RequestPart(required = false, name = "files") List<MultipartFile> files, String path) {


        return;
    }


}
