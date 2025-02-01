package org.mrshoffen.cloudstorage.user.http.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.user.model.dto.UserCreateDto;
import org.mrshoffen.cloudstorage.user.model.dto.UserResponseDto;
import org.mrshoffen.cloudstorage.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserRegistrationController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserCreateDto dto) {

        UserResponseDto createdUser = service.create(dto);

        return ResponseEntity
                .created(
                        UriComponentsBuilder
                                .fromPath("/api/v1/users/{userId}")
                                .build(Map.of("userId", createdUser.getId()))
                )
                .body(createdUser);
    }


}
