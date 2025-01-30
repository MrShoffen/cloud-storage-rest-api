package org.mrshoffen.cloudstorage.security.common.service;

import lombok.RequiredArgsConstructor;
import org.mrshoffen.cloudstorage.security.common.entity.StorageUserDetails;
import org.mrshoffen.cloudstorage.storage.entity.StorageUser;
import org.mrshoffen.cloudstorage.storage.repositroy.StorageUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageUserDetailsService implements UserDetailsService {

    private final StorageUserRepository storageUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        StorageUser user = storageUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return new StorageUserDetails(user);
    }
}
