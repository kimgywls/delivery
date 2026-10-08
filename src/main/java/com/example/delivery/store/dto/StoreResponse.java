package com.example.delivery.store.dto;

import com.example.delivery.store.entity.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record StoreResponse(
        Long id,
        String name,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {

    public static StoreResponse from(Store store) {
        return new StoreResponse(store.getId(), store.getName(), store.getCreatedAt(), store.getUpdatedAt());
    }
}
