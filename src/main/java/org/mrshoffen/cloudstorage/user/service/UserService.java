package org.mrshoffen.cloudstorage.user.service;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.user.model.dto.UserCreateDto;
import org.mrshoffen.cloudstorage.user.model.dto.UserInfoEditDto;
import org.mrshoffen.cloudstorage.user.model.dto.UserPasswordEditDto;
import org.mrshoffen.cloudstorage.user.model.dto.UserResponseDto;
import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.mrshoffen.cloudstorage.user.events.publisher.UserEventPublisher;
import org.mrshoffen.cloudstorage.user.exception.IncorrectPasswordException;
import org.mrshoffen.cloudstorage.user.exception.UserAlreadyExistsException;
import org.mrshoffen.cloudstorage.user.exception.UserNotFoundException;
import org.mrshoffen.cloudstorage.user.mapper.UserMapper;
import org.mrshoffen.cloudstorage.user.repositroy.StorageUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEventPublisher userEventPublisher;

    private final PasswordEncoder passwordEncoder;

    private final StorageUserRepository userRepository;

    private final UserMapper userMapper;


    @Transactional
    public UserResponseDto updateUserProfile(Long userId, UserInfoEditDto userInfoEditDto) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new UserNotFoundException("User with id '%s' not found".formatted(userId)));

        String oldUsername = user.getUsername();
        String newUsername = userInfoEditDto.newUsername();
        String newAvatarUrl = userInfoEditDto.newAvatarUrl();

        if (!oldUsername.equals(newUsername)) {
            checkForOccupiedUsername(newUsername);
        }

        user.setUsername(newUsername);
        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);

        userEventPublisher.publishUserInfoUpdateEvent(oldUsername, user);

        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto updateUserPassword(Long userId, UserPasswordEditDto userPasswordEditDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id '%s' not found".formatted(userId)));

        String oldPassword = userPasswordEditDto.oldPassword();
        String newPassword = userPasswordEditDto.newPassword();

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IncorrectPasswordException("Incorrect password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        userEventPublisher.publishUserPasswordUpdateEvent(user.getUsername());

        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto create(UserCreateDto userCreateDto) {
        checkForOccupiedUsername(userCreateDto.username());

        User userForSave = userMapper.toEntity(userCreateDto);
        userRepository.save(userForSave);

        return userMapper.toDto(userForSave);
    }

    private void checkForOccupiedUsername(String username) {
        userRepository.findByUsernameIgnoreCase(username)
                .ifPresent(u -> {
                    throw new UserAlreadyExistsException("User with username '%s' already exists!"
                            .formatted(u.getUsername()));
                });
    }
}
