package com.example.delivery.order.service;

import com.example.delivery.common.exception.ForbiddenException;
import com.example.delivery.common.exception.NotFoundException;
import com.example.delivery.member.entity.Member;
import com.example.delivery.member.repository.MemberRepository;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.order.dto.OrderRequest;
import com.example.delivery.order.dto.OrderResponse;
import com.example.delivery.order.dto.OrderStatusRequest;
import com.example.delivery.order.entity.Order;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.security.AuthMember;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderRequest request) {
        Member customer = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
        Menu menu = menuRepository.findByIdAndDeletedFalse(request.menuId())
                .orElseThrow(() -> new NotFoundException("메뉴를 찾을 수 없습니다."));

        // 총액(메뉴 가격 × 수량)은 Order 생성자에서 계산해 저장한다.
        Order order = new Order(customer, menu, request.quantity(), request.deliveryAddress());
        return OrderResponse.from(orderRepository.save(order));
    }

    // 기존 주문 조회에는 메뉴의 삭제 여부 조건을 넣지 않는다.
    public List<OrderResponse> getOrders(AuthMember authMember) {
        List<Order> orders = switch (authMember.role()) {
            case CUSTOMER -> orderRepository.findAllByCustomer_Id(authMember.id());
            case OWNER -> orderRepository.findAllByMenu_Owner_Id(authMember.id());
        };

        return orders.stream()
                .map(OrderResponse::from)
                .toList();
    }

    // CUSTOMER는 본인 주문, OWNER는 본인 메뉴에 들어온 주문만 조회할 수 있다.
    public OrderResponse getOrder(AuthMember authMember, Long orderId) {
        Order order = findOrder(orderId);
        boolean accessible = switch (authMember.role()) {
            case CUSTOMER -> isOrderedBy(order, authMember.id());
            case OWNER -> isMenuOwnedBy(order, authMember.id());
        };
        if (!accessible) {
            throw new ForbiddenException("본인 주문 또는 본인 메뉴의 주문만 조회할 수 있습니다.");
        }

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long memberId, Long orderId) {
        Order order = findOrder(orderId);
        if (!isOrderedBy(order, memberId)) {
            throw new ForbiddenException("본인 주문만 취소할 수 있습니다.");
        }

        order.cancel();
        // 응답에 갱신된 updatedAt을 담기 위해 DTO 변환 전에 flush해 @LastModifiedDate를 먼저 반영한다.
        orderRepository.flush();
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse changeOrderStatus(Long memberId, Long orderId, OrderStatusRequest request) {
        Order order = findOrder(orderId);
        if (!isMenuOwnedBy(order, memberId)) {
            throw new ForbiddenException("본인 메뉴의 주문만 변경할 수 있습니다.");
        }

        switch (request.status()) {
            case ACCEPTED -> order.accept();
            case DELIVERED -> order.deliver();
            default -> throw new IllegalStateException("검증되지 않은 상태값입니다: " + request.status());
        }
        orderRepository.flush();
        return OrderResponse.from(order);
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다."));
    }

    private boolean isOrderedBy(Order order, Long memberId) {
        return order.getCustomer().getId().equals(memberId);
    }

    // 메뉴가 삭제되었더라도 기존 주문은 처리할 수 있도록 메뉴의 deleted 여부는 확인하지 않는다.
    private boolean isMenuOwnedBy(Order order, Long memberId) {
        return order.getMenu().getOwner().getId().equals(memberId);
    }
}
