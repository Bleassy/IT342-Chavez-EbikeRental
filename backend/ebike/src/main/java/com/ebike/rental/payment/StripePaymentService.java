package com.ebike.rental.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.WebhookEndpoint;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.WebhookEndpointCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripePaymentService {

    @Value("${stripe.api.key:sk_test_default}")
    private String stripeApiKey;

    @Value("${stripe.publishable.key:pk_test_default}")
    private String stripePublishableKey;

    @Value("${stripe.enabled:true}")
    private boolean stripeEnabled;

    public StripePaymentService() {
        // Constructor
    }

    /**
     * Initialize Stripe API key
     */
    private void initializeStripe() {
        if (!stripeApiKey.equals("sk_test_default") && !stripeApiKey.isEmpty()) {
            Stripe.apiKey = stripeApiKey;
        }
    }

    /**
     * Create a Stripe Customer
     */
    public String createStripeCustomer(String email, String name) throws StripeException {
        if (!stripeEnabled) {
            throw new IllegalStateException("Stripe is not enabled");
        }

        initializeStripe();

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .setName(name)
                .build();

        Customer customer = Customer.create(params);
        return customer.getId();
    }

    /**
     * Create a Payment Intent for booking payment
     */
    public Map<String, Object> createPaymentIntent(Long bookingId, Double amount, String customerId) throws StripeException {
        if (!stripeEnabled) {
            throw new IllegalStateException("Stripe is not enabled");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        initializeStripe();

        // Amount in cents (Stripe uses smallest currency unit)
        long amountInCents = Math.round(amount * 100);

        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")  // Use USD for international support, can change to "php" after verifying Stripe account supports it
                .setDescription("eBike Booking #" + bookingId)
                .putMetadata("booking_id", bookingId.toString());

        // Only set customer if it appears to be a valid Stripe customer ID (starts with "cus_")
        if (customerId != null && !customerId.isEmpty() && customerId.startsWith("cus_")) {
            paramsBuilder.setCustomer(customerId);
        }

        // Enable automatic payment methods for card and other payment methods
        paramsBuilder.setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
        );

        PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());
        response.put("paymentIntentId", paymentIntent.getId());
        response.put("amount", amount);
        response.put("currency", "USD");
        response.put("status", paymentIntent.getStatus());

        return response;
    }

    /**
     * Retrieve Payment Intent status
     */
    public Map<String, Object> getPaymentIntentStatus(String paymentIntentId) throws StripeException {
        if (!stripeEnabled) {
            throw new IllegalStateException("Stripe is not enabled");
        }

        initializeStripe();

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        Map<String, Object> response = new HashMap<>();
        response.put("paymentIntentId", paymentIntent.getId());
        response.put("status", paymentIntent.getStatus());
        response.put("amount", paymentIntent.getAmount() / 100.0);  // Convert from cents
        response.put("currency", paymentIntent.getCurrency());
        response.put("metadata", paymentIntent.getMetadata());

        return response;
    }

    /**
     * Check if Stripe is enabled
     */
    public boolean isStripeEnabled() {
        return stripeEnabled;
    }

    /**
     * Get Stripe Publishable Key (safe to send to frontend)
     */
    public String getPublishableKey() {
        return stripePublishableKey;
    }

    /**
     * Refund a payment
     */
    public Map<String, Object> refundPayment(String paymentIntentId) throws StripeException {
        if (!stripeEnabled) {
            throw new IllegalStateException("Stripe is not enabled");
        }

        initializeStripe();

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        if (!"succeeded".equals(paymentIntent.getStatus())) {
            throw new IllegalStateException("Can only refund succeeded payments");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("payment_intent", paymentIntentId);

        com.stripe.model.Refund refund = com.stripe.model.Refund.create(params);

        Map<String, Object> response = new HashMap<>();
        response.put("refundId", refund.getId());
        response.put("status", refund.getStatus());
        response.put("amount", refund.getAmount() / 100.0);

        return response;
    }
}
