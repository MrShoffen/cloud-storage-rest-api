package org.mrshoffen.cloudstorage.user.service;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.user.dto.StorageUserEditDto;
import org.mrshoffen.cloudstorage.user.dto.StorageUserResponseDto;
import org.mrshoffen.cloudstorage.user.entity.StorageUser;
import org.mrshoffen.cloudstorage.user.exception.UserAlreadyExistsException;
import org.mrshoffen.cloudstorage.user.exception.UserNotFoundException;
import org.mrshoffen.cloudstorage.user.mapper.StorageUserMapper;
import org.mrshoffen.cloudstorage.user.repositroy.StorageUserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StorageUserService {

    private final ApplicationEventPublisher eventPublisher;

    private final StorageUserRepository userRepository;

    private final StorageUserMapper userMapper;


    @Transactional
    public StorageUserResponseDto updateUserProfile(Long userId, StorageUserEditDto userProfileEditDto) {
        StorageUser user = userRepository.findById(userId).
                orElseThrow(() -> new UserNotFoundException("User with id '%s' not found".formatted(userId)));

        String newUsername = userProfileEditDto.newUsername();
        String newAvatarUrl = userProfileEditDto.newAvatarUrl();

        if (!user.getUsername().equals(newUsername)) {
            checkForOccupiedUsername(newUsername);
        }

        user.setUsername(newUsername);
        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);

        eventPublisher.publishEvent(user);
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
