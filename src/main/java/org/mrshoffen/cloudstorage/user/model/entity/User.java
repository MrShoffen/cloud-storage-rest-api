package org.mrshoffen.cloudstorage.user.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data //в данной модели безопасно
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "storage_plan")
    @Enumerated(EnumType.STRING)
    private StoragePlan storagePlan = StoragePlan.BASIC;
}
