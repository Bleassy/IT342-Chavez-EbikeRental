package com.ebike.rental.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByBookingId(Long bookingId);
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);
    List<Payment> findByPaymentMethod(Payment.PaymentMethod method);
}
