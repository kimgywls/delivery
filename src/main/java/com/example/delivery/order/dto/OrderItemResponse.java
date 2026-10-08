package com.example.delivery.order.dto;

import com.example.delivery.order.entity.OrderItem;

public record OrderItemResponse(
        Long id,
        Long menuId,
        String menuName,
        Long unitPrice,
        Integer quantity,
        Long subtotal
) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getMenu().getId(),
                item.getMenuName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.subtotal());
    }
}
