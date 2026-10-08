package com.example.delivery.payment.entity;

import com.example.delivery.common.BaseEntity;
import com.example.delivery.order.entity.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    public Payment(Order order, PaymentMethod method) {
        this.order = order;
        this.amount = order.getTotalAmount();
        this.method = method;
        this.status = PaymentStatus.COMPLETED;
    }

    /**
     * 결제 기록을 취소 상태로 바꾼다. 실제 환불은 하지 않으며, 금액·수단·생성 시각은 그대로 남는다.
     */
    public void cancel() {
        if (this.status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("완료된 결제만 취소할 수 있습니다. paymentId=" + this.id);
        }
        this.status = PaymentStatus.CANCELED;
    }
}
