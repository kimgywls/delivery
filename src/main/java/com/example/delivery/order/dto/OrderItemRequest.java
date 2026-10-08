package com.example.delivery.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "메뉴 ID는 필수입니다.")
        @Positive(message = "메뉴 ID는 1 이상이어야 합니다.")
        Long menuId,

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 1 이상이어야 합니다.")
        Integer quantity
) {
}
