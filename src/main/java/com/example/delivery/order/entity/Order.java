package com.example.delivery.order.entity;

import com.example.delivery.common.BaseEntity;
import com.example.delivery.common.exception.InvalidOrderStatusException;
import com.example.delivery.common.exception.OrderCancelTimeExpiredException;
import com.example.delivery.member.entity.Member;
import com.example.delivery.store.entity.Store;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    // 주문은 한 가게에만 한다. 사장님 소유권은 order.store.owner로 확인한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 외래키(order_items.order_id)는 OrderItem이 관리한다. 주문 생성 시 상품도 함께 저장(PERSIST)한다.
    // 주문·결제 기록은 지우지 않으므로 REMOVE와 orphanRemoval은 두지 않는다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false, length = 500)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /**
     * 한 가게의 상품 1개 이상으로 주문을 만든다. 총액은 상품 금액의 합으로 계산한다.
     * 주문이 만들어진 뒤에는 상품을 추가하지 않는다(결제 금액이 주문 총액이므로).
     */
    public Order(Member customer, Store store, List<OrderItem> items, String deliveryAddress) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문 상품은 1개 이상이어야 합니다.");
        }
        this.customer = customer;
        this.store = store;
        this.totalAmount = 0L;
        items.forEach(this::addItem);
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.REQUESTED;
    }

    // 양쪽 객체 참조를 함께 설정하고 총액에 반영한다.
    private void addItem(OrderItem item) {
        if (!Objects.equals(item.getMenu().getStore().getId(), store.getId())) {
            throw new IllegalArgumentException("다른 가게의 메뉴는 같은 주문에 담을 수 없습니다.");
        }
        item.assignOrder(this);
        this.items.add(item);
        this.totalAmount += item.subtotal();
    }

    public void pay() {
        changeStatus(OrderStatus.REQUESTED, OrderStatus.PAID);
    }

    /**
     * 상태(REQUESTED 또는 PAID)를 먼저 확인한 뒤, 생성 시각부터 5분 이내인지 확인한다.
     * 정확히 5분인 시점까지는 허용한다. now는 호출하는 쪽에서 한 번 구해 전달한다.
     * PAID 주문의 결제 기록 취소는 이 메서드를 호출한 Service가 같은 트랜잭션에서 처리한다.
     */
    public void cancel(LocalDateTime now) {
        if (this.status != OrderStatus.REQUESTED && this.status != OrderStatus.PAID) {
            throw new InvalidOrderStatusException(this.status, OrderStatus.CANCELED);
        }
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

    /**
     * 사장님의 주문 거절. 결제 완료(PAID) 주문만 거절할 수 있고, 손님 취소의 5분 제한은 적용하지 않는다.
     * 결제 기록 취소는 이 메서드를 호출한 Service가 같은 트랜잭션에서 처리한다.
     */
    public void reject() {
        if (this.status != OrderStatus.PAID) {
            throw new InvalidOrderStatusException("결제 완료 상태의 주문만 거절할 수 있습니다.");
        }
        this.status = OrderStatus.REJECTED;
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
