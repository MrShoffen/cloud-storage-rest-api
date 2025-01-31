package org.mrshoffen.cloudstorage.user.service;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.user.dto.UserEditDto;
import org.mrshoffen.cloudstorage.user.dto.UserResponseDto;
import org.mrshoffen.cloudstorage.user.entity.User;
import org.mrshoffen.cloudstorage.user.events.publisher.UserEventPublisher;
import org.mrshoffen.cloudstorage.user.exception.UserAlreadyExistsException;
import org.mrshoffen.cloudstorage.user.exception.UserNotFoundException;
import org.mrshoffen.cloudstorage.user.mapper.UserMapper;
import org.mrshoffen.cloudstorage.user.repositroy.StorageUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserEventPublisher userEventPublisher;

    private final StorageUserRepository userRepository;

    private final UserMapper userMapper;


    @Transactional
    public UserResponseDto updateUserProfile(Long userId, UserEditDto userProfileEditDto) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new UserNotFoundException("User with id '%s' not found".formatted(userId)));

        String oldUsername = user.getUsername();
        String newUsername = userProfileEditDto.newUsername();
        String newAvatarUrl = userProfileEditDto.newAvatarUrl();

        if (!oldUsername.equals(newUsername)) {
            checkForOccupiedUsername(newUsername);
        }

        user.setUsername(newUsername);
        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);

        userEventPublisher.publishUserUpdateEvent(oldUsername, user);

        return userMapper.toDto(user);
    }


    private void checkForOccupiedUsername(String username) {
        userRepository.findByUsernameIgnoreCase(username)
                .ifPresent(u -> {
                    throw new UserAlreadyExistsException("User with username '%s' already exists!"
                            .formatted(u.getUsername()));
                });
    }
}
