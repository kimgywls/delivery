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
import com.example.delivery.order.entity.OrderStatus;
import com.example.delivery.order.repository.OrderRepository;
import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import com.example.delivery.payment.repository.PaymentRepository;
import com.example.delivery.security.AuthMember;
import java.time.LocalDateTime;
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
    private final PaymentRepository paymentRepository;

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

        boolean paid = order.getStatus() == OrderStatus.PAID;
        order.cancel(LocalDateTime.now());   // 상태·시간 규칙을 통과하지 못하면 409
        if (paid) {
            cancelCompletedPayment(order);
        }
        // 응답에 갱신된 updatedAt을 담기 위해 DTO 변환 전에 flush해 @LastModifiedDate를 먼저 반영한다.
        orderRepository.flush();
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse rejectOrder(Long memberId, Long orderId) {
        Order order = findOrder(orderId);
        if (!isMenuOwnedBy(order, memberId)) {
            throw new ForbiddenException("본인 메뉴의 주문만 거절할 수 있습니다.");
        }

        order.reject();                      // PAID가 아니면 409
        cancelCompletedPayment(order);       // 결제 후 취소와 같은 규칙으로 완료된 결제 1건을 CANCELED로 변경
        orderRepository.flush();
        return OrderResponse.from(order);
    }

    // PAID 주문에는 COMPLETED 결제가 정확히 1건 있어야 한다. 아니면 데이터 오류로 보고 전체를 롤백한다.
    private void cancelCompletedPayment(Order order) {
        List<Payment> payments = paymentRepository.findAllByOrder_IdAndStatus(order.getId(), PaymentStatus.COMPLETED);
        if (payments.size() != 1) {
            throw new IllegalStateException(
                    "결제 완료 주문의 완료된 결제 기록이 1건이 아닙니다. orderId=" + order.getId() + ", count=" + payments.size());
        }
        payments.getFirst().cancel();
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
