package com.example.delivery.payment.repository;

import com.example.delivery.payment.entity.Payment;
import com.example.delivery.payment.entity.PaymentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByOrder_Id(Long orderId);

    List<Payment> findAllByOrder_IdAndStatus(Long orderId, PaymentStatus status);
}
