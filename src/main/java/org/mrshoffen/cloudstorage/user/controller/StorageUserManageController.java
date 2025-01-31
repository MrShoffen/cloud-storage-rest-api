package org.mrshoffen.cloudstorage.user.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.user.dto.StorageUserEditDto;
import org.mrshoffen.cloudstorage.user.dto.StorageUserResponseDto;
import org.mrshoffen.cloudstorage.user.entity.StorageUser;
import org.mrshoffen.cloudstorage.user.mapper.StorageUserMapper;
import org.mrshoffen.cloudstorage.user.service.StorageUserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class StorageUserManageController {


    private final StorageUserService service;

    private final StorageUserMapper mapper;

    @GetMapping
    public ResponseEntity<StorageUserResponseDto> getUser(@AuthenticationPrincipal(expression = "getUser") StorageUser user) {
        return ResponseEntity.ok(mapper.toDto(user));
    }


    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;


    //todo add validation

    //todo update all sessions (or invalidate)
    @PatchMapping("/profile")
    public ResponseEntity<StorageUserResponseDto> updateUserProfile(@AuthenticationPrincipal(expression = "getUser") StorageUser user,
                                                                    @Valid @RequestBody StorageUserEditDto editDto) {

        StorageUserResponseDto updatedUser = service.updateUserProfile(user.getId(), editDto);

        return ResponseEntity.ok(updatedUser);
    }
}
