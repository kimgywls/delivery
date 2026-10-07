package com.example.delivery.order.entity;

import com.example.delivery.common.BaseEntity;
import com.example.delivery.common.exception.InvalidOrderStatusException;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

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

    public void cancel() {
        changeStatus(OrderStatus.REQUESTED, OrderStatus.CANCELED);
    }

    public void accept() {
        changeStatus(OrderStatus.PAID, OrderStatus.ACCEPTED);
    }

    public void deliver() {
        changeStatus(OrderStatus.ACCEPTED, OrderStatus.DELIVERED);
    }

    private void changeStatus(OrderStatus expected, OrderStatus next) {
        if (this.status != expected) {
            throw new InvalidOrderStatusException(this.status, next);
        }
        this.status = next;
    }
}
