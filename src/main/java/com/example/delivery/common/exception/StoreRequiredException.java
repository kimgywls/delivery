package com.example.delivery.common.exception;

public class StoreRequiredException extends RuntimeException {

    public StoreRequiredException() {
        super("가게를 먼저 등록해야 합니다.");
    }
}
