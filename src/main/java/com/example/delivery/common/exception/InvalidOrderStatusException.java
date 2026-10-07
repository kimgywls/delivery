package com.example.delivery.common.exception;

import com.example.delivery.order.entity.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(OrderStatus current, OrderStatus next) {
        super("주문 상태를 " + current + "에서 " + next + "(으)로 변경할 수 없습니다.");
    }
}
