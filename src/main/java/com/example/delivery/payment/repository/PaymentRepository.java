package com.example.delivery.payment.repository;

import com.example.delivery.payment.entity.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrder_Id(Long orderId);
}
