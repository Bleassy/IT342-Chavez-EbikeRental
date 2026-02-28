package com.ebike.rental.controller;

import com.ebike.rental.entity.Payment;
import com.ebike.rental.dto.ApiResponse;
import com.ebike.rental.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> processPayment(
            @RequestParam Long bookingId,
            @RequestParam Payment.PaymentMethod paymentMethod) {
        try {
            Payment payment = paymentService.processPayment(bookingId, paymentMethod);
            if (payment != null) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(true, "Payment processed successfully", payment));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Invalid booking ID"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to process payment: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Payment>>> getAllPayments() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            return ResponseEntity.ok(new ApiResponse<>(true, "Payments retrieved successfully", payments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve payments: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(@PathVariable Long id) {
        try {
            Optional<Payment> payment = paymentService.getPaymentById(id);
            if (payment.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment retrieved successfully", payment.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Payment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve payment: " + e.getMessage()));
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByTransactionId(@PathVariable String transactionId) {
        try {
            Optional<Payment> payment = paymentService.getPaymentByTransactionId(transactionId);
            if (payment.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment retrieved successfully", payment.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Payment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve payment: " + e.getMessage()));
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByBookingId(@PathVariable Long bookingId) {
        try {
            Optional<Payment> payment = paymentService.getPaymentByBookingId(bookingId);
            if (payment.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment retrieved successfully", payment.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Payment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve payment: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<String>> completePayment(@PathVariable Long id) {
        try {
            if (paymentService.completePayment(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment completed successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Payment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to complete payment: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<String>> failPayment(@PathVariable Long id) {
        try {
            if (paymentService.failPayment(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment marked as failed", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Payment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update payment: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<String>> refundPayment(@PathVariable Long id) {
        try {
            if (paymentService.refundPayment(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment refunded successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Payment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to refund payment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePayment(@PathVariable Long id) {
        try {
            if (paymentService.deletePayment(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Payment deleted successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "Payment not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete payment: " + e.getMessage()));
        }
    }
}
