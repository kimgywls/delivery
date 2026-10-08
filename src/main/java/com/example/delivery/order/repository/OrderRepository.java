package com.example.delivery.order.repository;

import com.example.delivery.order.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 목록 응답에 주문 상품이 필요하므로 상품을 함께 조회해 주문마다 상품을 따로 조회하는 N+1을 막는다.
    @EntityGraph(attributePaths = "items")
    List<Order> findAllByCustomer_Id(Long customerId);

    @EntityGraph(attributePaths = "items")
    List<Order> findAllByStore_Owner_Id(Long ownerId);
}
