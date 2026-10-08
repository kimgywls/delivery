package com.example.delivery.order.dto;

import com.example.delivery.order.entity.Order;
import com.example.delivery.order.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record OrderResponse(
        Long id,
        Long storeId,
        List<OrderItemResponse> items,
        Long totalAmount,
        String deliveryAddress,
        OrderStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .sorted(Comparator.comparing(OrderItemResponse::id))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStore().getId(),
                items,
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
