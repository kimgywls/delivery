package com.example.delivery.common.exception;

public class MixedStoreOrderException extends RuntimeException {

    public MixedStoreOrderException() {
        super("한 주문에는 같은 가게의 메뉴만 담을 수 있습니다.");
    }
}
