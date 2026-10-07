package com.example.delivery.order.dto;

import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long menuId,
        Integer quantity,
        Long totalAmount,
        String deliveryAddress,
        OrderStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getMenu().getId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
