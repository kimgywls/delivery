package com.example.delivery.payment.dto;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentMethod;
import com.example.delivery.payment.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long amount,
        PaymentMethod method,
        PaymentStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }
}
