package com.ebike.rental.payment;

import com.ebike.rental.booking.BookingRepository;
import com.ebike.rental.dto.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private GCashPaymentService gcashPaymentService;

    @Autowired
    private StripePaymentService stripePaymentService;

    @Autowired
    private BookingRepository bookingRepository;

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

    @PostMapping("/gcash/initiate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiateGCashPayment(
            @RequestParam Long bookingId) {
        try {
            Optional<com.ebike.rental.booking.Booking> booking = 
                    bookingRepository.findById(bookingId);
            
            if (booking.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Invalid booking ID"));
            }

            Double amount = booking.get().getTotalPrice().doubleValue();
            String description = "E-Bike Rental - Booking #" + bookingId;

            Map<String, Object> result = gcashPaymentService.initiatePayment(
                    amount, description, bookingId);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(
                        new ApiResponse<>(true, "GCash payment initiated", result));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse<>(false, (String) result.get("message")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to initiate GCash payment: " + e.getMessage()));
        }
    }

    @PostMapping("/gcash/callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> gcashPaymentCallback(
            @RequestBody Map<String, Object> webhookData) {
        try {
            Map<String, Object> result = gcashPaymentService.verifyPaymentCallback(webhookData);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(
                        new ApiResponse<>(true, "Callback processed successfully", result));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, (String) result.get("message")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to process callback: " + e.getMessage()));
        }
    }

    @GetMapping("/gcash/status")
    public ResponseEntity<ApiResponse<String>> getGCashStatus() {
        try {
            String status = gcashPaymentService.getPayMongoStatus();
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "PayMongo status retrieved", status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve PayMongo status: " + e.getMessage()));
        }
    }

    @GetMapping("/gcash/payment/{paymentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGCashPaymentStatus(
            @PathVariable String paymentId) {
        try {
            Map<String, Object> result = gcashPaymentService.getPaymentStatus(paymentId);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(
                        new ApiResponse<>(true, "Payment status retrieved", result));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, (String) result.get("message")));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving payment status: " + e.getMessage()));
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

    // ==================== STRIPE PAYMENT ENDPOINTS ====================

    @GetMapping("/stripe/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStripeConfig() {
        try {
            if (!stripePaymentService.isStripeEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ApiResponse<>(false, "Stripe is not enabled"));
            }

            Map<String, Object> config = new java.util.HashMap<>();
            config.put("publishableKey", stripePaymentService.getPublishableKey());
            config.put("enabled", true);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Stripe configuration retrieved", config));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get Stripe config: " + e.getMessage()));
        }
    }

    @PostMapping("/stripe/create-payment-intent")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStripePaymentIntent(
            @RequestParam Long bookingId,
            @RequestParam String customerId) {
        try {
            if (!stripePaymentService.isStripeEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ApiResponse<>(false, "Stripe is not enabled"));
            }

            Optional<com.ebike.rental.booking.Booking> booking = bookingRepository.findById(bookingId);
            if (booking.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Invalid booking ID"));
            }

            Double amount = booking.get().getTotalPrice().doubleValue();
            Map<String, Object> paymentIntent = stripePaymentService.createPaymentIntent(
                    bookingId, amount, customerId);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Payment intent created successfully", paymentIntent));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create payment intent: " + e.getMessage()));
        }
    }

    @GetMapping("/stripe/payment-intent/{paymentIntentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStripePaymentIntentStatus(
            @PathVariable String paymentIntentId) {
        try {
            if (!stripePaymentService.isStripeEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ApiResponse<>(false, "Stripe is not enabled"));
            }

            Map<String, Object> paymentIntentStatus = stripePaymentService.getPaymentIntentStatus(paymentIntentId);

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Payment intent status retrieved", paymentIntentStatus));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to get payment intent status: " + e.getMessage()));
        }
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload) {
        try {
            // Webhook handling logic will be implemented to update payment status
            // when Stripe notifies of payment success/failure
            System.out.println("Stripe webhook received");
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing failed: " + e.getMessage());
        }
    }
}

