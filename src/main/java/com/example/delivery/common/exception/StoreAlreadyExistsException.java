package com.example.delivery.common.exception;

public class StoreAlreadyExistsException extends RuntimeException {

    public StoreAlreadyExistsException() {
        super("이미 가게를 등록했습니다.");
    }
}
