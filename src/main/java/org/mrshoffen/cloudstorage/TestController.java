package org.mrshoffen.cloudstorage;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.security.service.SecurityContextService;
import org.mrshoffen.cloudstorage.user.entity.User;
import org.mrshoffen.cloudstorage.user.repositroy.StorageUserRepository;
import org.mrshoffen.cloudstorage.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {


    private final StorageUserRepository storageUserRepository;

    @GetMapping("/test")
    public ResponseEntity<String> sessionTest(
            HttpServletRequest request, @AuthenticationPrincipal(expression = "getUser") User user) {

        return ResponseEntity.ok(user.getUsername());
    }

    private final SecurityContextService securityContextService;

    private final UserService userService;

    @PostMapping("/test_update")
    public ResponseEntity<?> updateUsername(
            Authentication authentication
    ) {
        StorageUserDetails storageUser = (StorageUserDetails) authentication.getPrincipal();
        storageUser.getUser().setUsername("penis");

        UsernamePasswordAuthenticationToken newAuth = UsernamePasswordAuthenticationToken
                .authenticated(storageUser, null, List.of());

        securityContextService.updateAuthInContext(newAuth);

//        newAuth.setDetails();

//        storageUserService.updateUser(new StorageUserEditDto(this, "new"));
        return ResponseEntity.ok().build();
    }

}
