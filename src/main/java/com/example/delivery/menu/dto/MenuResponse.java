package com.example.delivery.menu.dto;

import com.example.delivery.menu.entity.Menu;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record MenuResponse(
        Long id,
        Long ownerId,
        String name,
        Long price,
        String description,

        // Entity와 DB는 마이크로초까지 저장하고, 응답만 초 단위로 내보낸다.
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getOwner().getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getCreatedAt(),
                menu.getUpdatedAt());
    }
}
