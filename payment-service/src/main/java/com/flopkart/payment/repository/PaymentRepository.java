package com.flopkart.payment.repository;

import com.flopkart.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderNumber(String orderNumber);

    Optional<Payment> findByPaymentReference(String paymentReference);

    boolean existsByOrderNumber(String orderNumber);
}
