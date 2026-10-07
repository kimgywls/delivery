package com.example.delivery.payment.service;

import com.example.delivery.common.exception.ForbiddenException;
import com.example.delivery.common.exception.NotFoundException;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.payment.dto.PaymentRequest;
import com.example.delivery.payment.dto.PaymentResponse;
import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 주문 상태 변경(PAID)과 결제 내역 저장을 하나의 트랜잭션으로 처리한다.
     * 둘 중 하나라도 실패하면 함께 롤백된다.
     */
    @Transactional
    public PaymentResponse pay(Long memberId, Long orderId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다."));
        if (!order.getCustomer().getId().equals(memberId)) {
            throw new ForbiddenException("본인 주문만 결제할 수 있습니다.");
        }

        // REQUESTED가 아니면 InvalidOrderStatusException(409)이 발생하고 결제 내역은 저장되지 않는다.
        order.pay();
        // 결제 금액은 요청이 아니라 주문에 저장된 totalAmount를 사용한다.
        Payment payment = paymentRepository.save(new Payment(order, request.method()));
        return PaymentResponse.from(payment);
    }
}
