package org.mrshoffen.cloudstorage.storage;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.storage.entity.StorageUser;
import org.mrshoffen.cloudstorage.storage.repositroy.StorageUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TestController {


    private final StorageUserRepository storageUserRepository;

    @GetMapping("/test")
    public ResponseEntity<String> sessionTest(
            HttpServletRequest request, @AuthenticationPrincipal(expression = "getUser") StorageUser user) {

        return ResponseEntity.ok(user.getUsername());
    }
}
