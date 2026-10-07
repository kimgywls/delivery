package com.example.delivery.payment.dto;

import com.example.delivery.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * 결제 요청. PaymentMethod에는 CARD만 있으므로 다른 값은 JSON 변환 단계에서 400으로 거절된다.
 */
public record PaymentRequest(
        @NotNull(message = "결제 수단은 필수입니다.")
        PaymentMethod method
) {
}
