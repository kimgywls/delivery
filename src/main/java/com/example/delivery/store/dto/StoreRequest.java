package com.example.delivery.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreRequest(
        @NotBlank(message = "가게 이름은 필수입니다.")
        @Size(max = 100, message = "가게 이름은 100자 이하여야 합니다.")
        String name
) {
}
