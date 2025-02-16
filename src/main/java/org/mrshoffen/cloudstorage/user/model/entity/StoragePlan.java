package org.mrshoffen.cloudstorage.user.model.entity;

import lombok.Getter;

@Getter
public enum StoragePlan {
    BASIC(1),
    STANDARD(2),
    PRO(4);

    private final long capacity; //GB

    StoragePlan(long capacity) {
        this.capacity = capacity;
    }
}
