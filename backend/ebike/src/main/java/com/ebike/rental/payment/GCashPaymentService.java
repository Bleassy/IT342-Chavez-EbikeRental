package com.ebike.rental.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;

@Service
public class GCashPaymentService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${paymongo.enabled:false}")
    private boolean paymongoEnabled;

    @Value("${paymongo.secret.key:}")
    private String paymongoSecretKey;

    @Value("${paymongo.public.key:}")
    private String paymongoPublicKey;

    @Value("${paymongo.api.url:https://api.paymongo.com/v1}")
    private String paymongoApiUrl;

    @Value("${app.callback.url:http://localhost:5173}")
    private String appCallbackUrl;

    /**
     * Initiate a GCash payment request via PayMongo
     * @param amount Payment amount in PHP
     * @param description Payment description
     * @param bookingId Reference/booking ID
     * @return Payment request details including checkout URL
     */
    public Map<String, Object> initiatePayment(Double amount, String description, Long bookingId) {
        Map<String, Object> response = new HashMap<>();
        
        if (!paymongoEnabled) {
            response.put("success", false);
            response.put("message", "PayMongo GCash payment is not enabled");
            response.put("status", "DISABLED");
            return response;
        }

        if (paymongoSecretKey.isEmpty()) {
            response.put("success", false);
            response.put("message", "PayMongo API credentials not configured");
            response.put("status", "NOT_CONFIGURED");
            return response;
        }

        try {
            // Create payment session with PayMongo
            String sessionId = createCheckoutSession(amount, description, bookingId);
            
            if (sessionId != null && !sessionId.isEmpty()) {
                response.put("success", true);
                response.put("message", "GCash payment initiated successfully");
                response.put("sessionId", sessionId);
                response.put("amount", amount);
                response.put("currency", "PHP");
                response.put("bookingId", bookingId);
                response.put("checkoutUrl", paymongoApiUrl.replace("/v1", "") + "/checkout/" + sessionId);
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Failed to create PayMongo checkout session");
                return response;
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error initiating GCash payment: " + e.getMessage());
            return response;
        }
    }

    /**
     * Create checkout session with PayMongo
     */
    private String createCheckoutSession(Double amount, String description, Long bookingId) {
        try {
            String auth = Base64.getEncoder().encodeToString((paymongoSecretKey + ":").getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + auth);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> lineItems = new HashMap<>();
            lineItems.put("currency", "PHP");
            lineItems.put("amount", (long) (amount * 100)); // PayMongo expects amount in cents
            lineItems.put("description", description);

            Map<String, Object> data = new HashMap<>();
            data.put("line_items", new Map[]{lineItems});
            data.put("success_url", appCallbackUrl + "/booking/success?booking=" + bookingId);
            data.put("cancel_url", appCallbackUrl + "/booking/cancel?booking=" + bookingId);
            data.put("reference_number", "BOOKING_" + bookingId + "_" + UUID.randomUUID().toString().substring(0, 8));
            data.put("payment_method_types", new String[]{"gcash"});
            data.put("customer_email", "customer@ebike.local");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    paymongoApiUrl + "/checkout_sessions",
                    entity,
                    Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("data")) {
                    Map<String, Object> sessionData = (Map<String, Object>) body.get("data");
                    return (String) sessionData.get("id");
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating PayMongo checkout session: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verify PayMongo payment callback/webhook
     * @param webhookData Data from PayMongo webhook
     * @return Verification result
     */
    public Map<String, Object> verifyPaymentCallback(Map<String, Object> webhookData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // PayMongo webhook structure
            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
            String type = (String) attributes.get("type");
            
            // Check if it's a payment.paid event
            if ("payment.paid".equals(type)) {
                Map<String, Object> paymentData = (Map<String, Object>) attributes.get("data");
                String paymentStatus = (String) paymentData.get("status");
                String paymentId = (String) paymentData.get("id");
                Long amount = (Long) paymentData.get("amount");
                
                response.put("success", true);
                response.put("message", "Payment verified successfully");
                response.put("paymentId", paymentId);
                response.put("status", paymentStatus);
                response.put("amount", amount / 100.0); // Convert from cents to PHP
                response.put("type", "payment.paid");
                
                return response;
            } else {
                response.put("success", false);
                response.put("message", "Not a payment.paid event: " + type);
                return response;
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Webhook verification failed: " + e.getMessage());
            return response;
        }
    }

    /**
     * Retrieve payment status from PayMongo
     */
    public Map<String, Object> getPaymentStatus(String paymentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String auth = Base64.getEncoder().encodeToString((paymongoSecretKey + ":").getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + auth);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> apiResponse = restTemplate.getForEntity(
                    paymongoApiUrl + "/payments/" + paymentId,
                    Map.class
            );

            if (apiResponse.getStatusCode().is2xxSuccessful() && apiResponse.getBody() != null) {
                Map<String, Object> body = apiResponse.getBody();
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
                
                response.put("success", true);
                response.put("paymentId", data.get("id"));
                response.put("status", attributes.get("status"));
                response.put("amount", attributes.get("amount"));
                response.put("currency", attributes.get("currency"));
                response.put("paymentMethod", attributes.get("payment_method_type"));
            } else {
                response.put("success", false);
                response.put("message", "Failed to retrieve payment status");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving payment status: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * Check if PayMongo GCash is enabled and configured
     */
    public boolean isPayMongoAvailable() {
        return paymongoEnabled && !paymongoSecretKey.isEmpty();
    }

    public String getPayMongoStatus() {
        if (!paymongoEnabled) {
            return "DISABLED";
        }
        if (paymongoSecretKey.isEmpty()) {
            return "NOT_CONFIGURED";
        }
        return "CONFIGURED";
    }
}
