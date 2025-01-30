package org.mrshoffen.cloudstorage.storage.repositroy;

import org.mrshoffen.cloudstorage.storage.entity.StorageUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageUserRepository extends JpaRepository<StorageUser, Long> {

    Optional<StorageUser> findByUsernameIgnoreCase(String username);

}
