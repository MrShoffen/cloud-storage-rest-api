package org.mrshoffen.cloudstorage.user.http.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.user.model.dto.UserInfoEditDto;
import org.mrshoffen.cloudstorage.user.model.dto.UserPasswordEditDto;
import org.mrshoffen.cloudstorage.user.model.dto.UserResponseDto;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.mrshoffen.cloudstorage.user.mapper.UserMapper;
import org.mrshoffen.cloudstorage.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserManageController {

    private final UserService service;
    private final UserMapper mapper;

    @GetMapping
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal(expression = "getUser") User user) {
        return ResponseEntity.ok(mapper.toDto(user));
    }


    @PatchMapping("/profile")
    public ResponseEntity<UserResponseDto> updateUserInfo(@AuthenticationPrincipal(expression = "getUser") User user,
                                                             @Valid @RequestBody UserInfoEditDto editDto) {

        UserResponseDto updatedUser = service.updateUserProfile(user.getId(), editDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/password")
    public ResponseEntity<UserResponseDto> updateUserPassword(@AuthenticationPrincipal(expression = "getUser") User user,
                                                              @Valid @RequestBody UserPasswordEditDto editDto){

        UserResponseDto updatedUser = service.updateUserPassword(user.getId(), editDto);

        return ResponseEntity.ok(updatedUser);
    }

}
