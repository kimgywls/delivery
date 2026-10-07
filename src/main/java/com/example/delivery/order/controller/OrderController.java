package com.example.delivery.order.controller;

import com.example.delivery.order.dto.OrderRequest;
import com.example.delivery.order.dto.OrderResponse;
import com.example.delivery.order.dto.OrderStatusRequest;
import com.example.delivery.order.service.OrderService;
import com.example.delivery.security.AuthMember;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@AuthenticationPrincipal AuthMember authMember,
                                                     @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(authMember.id(), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@AuthenticationPrincipal AuthMember authMember) {
        return ResponseEntity.ok(orderService.getOrders(authMember));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@AuthenticationPrincipal AuthMember authMember,
                                                     @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(authMember.id(), orderId));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> changeOrderStatus(@AuthenticationPrincipal AuthMember authMember,
                                                           @PathVariable Long orderId,
                                                           @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(orderService.changeOrderStatus(authMember.id(), orderId, request));
    }
}
