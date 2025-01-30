package org.mrshoffen.cloudstorage.user.repositroy;

import org.mrshoffen.cloudstorage.user.entity.StorageUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageUserRepository extends JpaRepository<StorageUser, Long> {

    Optional<StorageUser> findByUsernameIgnoreCase(String username);

}
