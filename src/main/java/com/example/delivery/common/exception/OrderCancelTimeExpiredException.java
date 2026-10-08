package com.example.delivery.common.exception;

public class OrderCancelTimeExpiredException extends RuntimeException {

    public OrderCancelTimeExpiredException() {
        super("주문 생성 후 5분이 지나 취소할 수 없습니다.");
    }
}
