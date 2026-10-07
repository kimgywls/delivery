package com.example.delivery.payment.controller;

import com.example.delivery.payment.dto.PaymentRequest;
import com.example.delivery.payment.dto.PaymentResponse;
import com.example.delivery.payment.service.PaymentService;
import com.example.delivery.security.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/orders/{orderId}/payments")
    public ResponseEntity<PaymentResponse> pay(@AuthenticationPrincipal AuthMember authMember,
                                               @PathVariable Long orderId,
                                               @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.pay(authMember.id(), orderId, request));
    }
}
