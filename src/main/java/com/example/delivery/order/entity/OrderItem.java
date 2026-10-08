package com.example.delivery.order.entity;

import com.example.delivery.common.BaseEntity;
import com.example.delivery.menu.entity.Menu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * 주문 상품. 주문 당시의 메뉴 이름과 단가를 복사해 두어, 메뉴가 바뀌거나 삭제되어도 주문 기록이 유지된다.
 * order_id 외래키를 가진 연관관계의 주인이다.
 */
@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false, length = 100)
    private String menuName;

    @Column(nullable = false)
    private Long unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    public OrderItem(Menu menu, Integer quantity) {
        this.menu = menu;
        this.menuName = menu.getName();
        this.unitPrice = menu.getPrice();
        this.quantity = quantity;
    }

    // 상품 금액 = 주문 당시 단가 × 수량
    public long subtotal() {
        return unitPrice * quantity;
    }

    // 양쪽 참조는 Order.addItem()에서만 함께 설정한다.
    void assignOrder(Order order) {
        this.order = order;
    }
}
