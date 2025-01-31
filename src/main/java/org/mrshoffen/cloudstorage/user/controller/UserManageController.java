package org.mrshoffen.cloudstorage.user.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.session.service.SessionService;
import org.mrshoffen.cloudstorage.user.dto.UserEditDto;
import org.mrshoffen.cloudstorage.user.dto.UserResponseDto;
import org.mrshoffen.cloudstorage.user.entity.User;
import org.mrshoffen.cloudstorage.user.mapper.UserMapper;
import org.mrshoffen.cloudstorage.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserManageController {

    private final UserService service;
    private final UserMapper mapper;

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal(expression = "getUser") User user) {

        sessionService.invalidateAllUserOtherSessions(user.getUsername());
        return ResponseEntity.ok(mapper.toDto(user));
    }


    @PatchMapping("/profile")
    public ResponseEntity<UserResponseDto> updateUserProfile(@AuthenticationPrincipal(expression = "getUser") User user,
                                                             @Valid @RequestBody UserEditDto editDto) {

        UserResponseDto updatedUser = service.updateUserProfile(user.getId(), editDto);
        return ResponseEntity.ok(updatedUser);
    }

}
