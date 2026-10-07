package com.example.delivery.order.repository;

import com.example.delivery.order.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByCustomer_Id(Long customerId);

    List<Order> findAllByMenu_Owner_Id(Long ownerId);
}
