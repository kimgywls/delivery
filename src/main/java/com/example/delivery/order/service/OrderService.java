package com.example.delivery.order.service;

import com.example.delivery.common.exception.NotFoundException;
import com.example.delivery.member.entity.Member;
import com.example.delivery.member.repository.MemberRepository;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.order.dto.OrderRequest;
import com.example.delivery.order.dto.OrderResponse;
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
}
