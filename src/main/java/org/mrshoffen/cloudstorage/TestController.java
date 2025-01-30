package org.mrshoffen.cloudstorage;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TestController {



    @GetMapping("/test")
    public ResponseEntity<String> sessionTest(HttpServletRequest request, @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(user.getUsername());
    }
}
