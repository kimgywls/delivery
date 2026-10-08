package com.example.delivery.order.entity;

import com.example.delivery.common.BaseEntity;
import com.example.delivery.common.exception.InvalidOrderStatusException;
import com.example.delivery.common.exception.OrderCancelTimeExpiredException;
import com.example.delivery.member.entity.Member;
import com.example.delivery.menu.entity.Menu;
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
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    // 주문 생성 후 취소할 수 있는 시간
    private static final Duration CANCEL_TIME_LIMIT = Duration.ofMinutes(5);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Member customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false, length = 500)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    public Order(Member customer, Menu menu, Integer quantity, String deliveryAddress) {
        this.customer = customer;
        this.menu = menu;
        this.quantity = quantity;
        this.totalAmount = menu.getPrice() * quantity;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.REQUESTED;
    }

    public void pay() {
        changeStatus(OrderStatus.REQUESTED, OrderStatus.PAID);
    }

    /**
     * 상태(REQUESTED)를 먼저 확인한 뒤, 생성 시각부터 5분 이내인지 확인한다.
     * 정확히 5분인 시점까지는 허용한다. now는 호출하는 쪽에서 한 번 구해 전달한다.
     */
    public void cancel(LocalDateTime now) {
        validateStatus(OrderStatus.REQUESTED, OrderStatus.CANCELED);
        if (now.isAfter(getCreatedAt().plus(CANCEL_TIME_LIMIT))) {
            throw new OrderCancelTimeExpiredException();
        }
        this.status = OrderStatus.CANCELED;
    }

    public void accept() {
        changeStatus(OrderStatus.PAID, OrderStatus.ACCEPTED);
    }

    public void deliver() {
        changeStatus(OrderStatus.ACCEPTED, OrderStatus.DELIVERED);
    }

    private void changeStatus(OrderStatus expected, OrderStatus next) {
        validateStatus(expected, next);
        this.status = next;
    }

    private void validateStatus(OrderStatus expected, OrderStatus next) {
        if (this.status != expected) {
            throw new InvalidOrderStatusException(this.status, next);
        }
    }
}
