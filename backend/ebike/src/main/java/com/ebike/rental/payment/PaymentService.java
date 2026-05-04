package com.ebike.rental.payment;

import com.ebike.rental.booking.Booking;
import com.ebike.rental.booking.BookingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Payment processPayment(Long bookingId, Payment.PaymentMethod paymentMethod) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);

        if (booking.isEmpty()) {
            return null;
        }

        Payment payment = new Payment();
        payment.setBooking(booking.get());
        payment.setAmount(booking.get().getTotalPrice());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setTransactionId(UUID.randomUUID().toString());

        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    public Optional<Payment> getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }

    public boolean completePayment(Long paymentId) {
        return paymentRepository.findById(paymentId).map(payment -> {
            payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
            paymentRepository.save(payment);
            return true;
        }).orElse(false);
    }

    public boolean failPayment(Long paymentId) {
        return paymentRepository.findById(paymentId).map(payment -> {
            payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            return true;
        }).orElse(false);
    }

    public boolean refundPayment(Long paymentId) {
        return paymentRepository.findById(paymentId).map(payment -> {
            payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            return true;
        }).orElse(false);
    }

    public boolean deletePayment(Long paymentId) {
        if (paymentRepository.existsById(paymentId)) {
            paymentRepository.deleteById(paymentId);
            return true;
        }
        return false;
    }
}
