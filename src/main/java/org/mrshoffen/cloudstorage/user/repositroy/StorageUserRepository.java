package org.mrshoffen.cloudstorage.user.repositroy;

import org.mrshoffen.cloudstorage.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

}
