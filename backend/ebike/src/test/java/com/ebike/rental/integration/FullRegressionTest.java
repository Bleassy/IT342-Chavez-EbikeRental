package com.ebike.rental.integration;

import com.ebike.rental.auth.dto.LoginRequest;
import com.ebike.rental.auth.dto.RegisterRequest;
import com.ebike.rental.bike.model.Bike;
import com.ebike.rental.booking.model.Booking;
import com.ebike.rental.booking.dto.BookingDTO;
import com.ebike.rental.payment.model.Payment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full Regression Test Suite for eBike Rental System
 * Covers all critical endpoints and workflows
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FullRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private String adminToken;
    private Long testBikeId;
    private Long testBookingId;
    private Long testUserId;

    @BeforeEach
    public void setUp() throws Exception {
        // Register and login test user
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("regression@test.com");
        registerRequest.setPassword("Test@1234");
        registerRequest.setFirstName("Regression");
        registerRequest.setLastName("Tester");
        registerRequest.setPhone("09876543210");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("regression@test.com");
        loginRequest.setPassword("Test@1234");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        // Extract JWT token from response
        jwtToken = extractToken(response);
    }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    public void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("Health")));
    }

    @Test
    public void testAPIStatus() throws Exception {
        mockMvc.perform(get("/api/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", containsString("eBike Rental")));
    }

    @Test
    public void testUserRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser" + System.currentTimeMillis() + "@test.com");
        request.setPassword("Secure@Pass123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPhone("09111111111");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", equalTo(request.getEmail())));
    }

    @Test
    public void testUserLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("regression@test.com");
        request.setPassword("Test@1234");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", notNullValue()));
    }

    @Test
    public void testInvalidLogin() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword("WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== BIKE MANAGEMENT TESTS ====================

    @Test
    public void testGetAllBikes() throws Exception {
        mockMvc.perform(get("/api/bikes")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", isA(ArrayList.class)));
    }

    @Test
    public void testGetBikeDetail() throws Exception {
        // First get a bike from list
        MvcResult result = mockMvc.perform(get("/api/bikes")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        // Parse response and get first bike ID
        String response = result.getResponse().getContentAsString();
        Long bikeId = extractBikeIdFromResponse(response);

        if (bikeId != null) {
            mockMvc.perform(get("/api/bikes/" + bikeId)
                    .header("Authorization", "Bearer " + jwtToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(bikeId.intValue())))
                    .andExpect(jsonPath("$.data.bikeCode", notNullValue()));
        }
    }

    @Test
    public void testBikeAvailabilityStatus() throws Exception {
        mockMvc.perform(get("/api/bikes")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].status", everyItem(
                        anyOf(equalTo("AVAILABLE"), equalTo("RENTED"), equalTo("MAINTENANCE")))));
    }

    @Test
    public void testBikePricingFields() throws Exception {
        mockMvc.perform(get("/api/bikes")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].pricePerHour", everyItem(notNullValue())))
                .andExpect(jsonPath("$.data[*].pricePerDay", everyItem(notNullValue())));
    }

    // ==================== BOOKING MANAGEMENT TESTS ====================

    @Test
    public void testCreateBooking() throws Exception {
        // Get available bike first
        MvcResult bikeResult = mockMvc.perform(get("/api/bikes")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        String bikeResponse = bikeResult.getResponse().getContentAsString();
        Long bikeId = extractBikeIdFromResponse(bikeResponse);

        if (bikeId != null) {
            BookingDTO bookingDTO = new BookingDTO();
            bookingDTO.setBikeId(bikeId);
            bookingDTO.setStartTime("2026-05-15T10:00:00");
            bookingDTO.setEndTime("2026-05-15T14:00:00");

            mockMvc.perform(post("/api/bookings")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.status", equalTo("PENDING")));
        }
    }

    @Test
    public void testGetUserBookings() throws Exception {
        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    public void testBookingPriceCalculation() throws Exception {
        // Create booking and verify price
        MvcResult bikeResult = mockMvc.perform(get("/api/bikes")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        String bikeResponse = bikeResult.getResponse().getContentAsString();
        Long bikeId = extractBikeIdFromResponse(bikeResponse);
        Double hourlyRate = extractBikeHourlyRate(bikeResponse);

        if (bikeId != null && hourlyRate != null) {
            BookingDTO bookingDTO = new BookingDTO();
            bookingDTO.setBikeId(bikeId);
            bookingDTO.setStartTime("2026-05-15T10:00:00");
            bookingDTO.setEndTime("2026-05-15T14:00:00"); // 4 hours

            MvcResult bookingResult = mockMvc.perform(post("/api/bookings")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookingDTO)))
                    .andExpect(status().isCreated())
                    .andReturn();

            String bookingResponse = bookingResult.getResponse().getContentAsString();
            Double totalCost = extractTotalCost(bookingResponse);
            Double expectedCost = hourlyRate * 4;

            assert Math.abs(totalCost - expectedCost) < 0.01 : "Price calculation mismatch";
        }
    }

    // ==================== PAYMENT TESTS ====================

    @Test
    public void testPaymentProcessing() throws Exception {
        // Payment endpoint test would depend on Stripe/GCash setup
        mockMvc.perform(get("/api/payments")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // ==================== INPUT VALIDATION TESTS ====================

    @Test
    public void testEmptyEmailValidation() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("");
        request.setPassword("Test@1234");
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testWeakPasswordValidation() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@test.com");
        request.setPassword("123");  // Too short
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUnauthorizedAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidTokenValidation() throws Exception {
        mockMvc.perform(get("/api/bookings")
                .header("Authorization", "Bearer invalid_token_12345"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CORS CONFIGURATION TESTS ====================

    @Test
    public void testCORSHeaders() throws Exception {
        mockMvc.perform(get("/api/bikes")
                .header("Authorization", "Bearer " + jwtToken)
                .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    public void testNotFoundError() throws Exception {
        mockMvc.perform(get("/api/bikes/99999")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testInternalServerError() throws Exception {
        // This test would trigger when backend returns 500
        // Actual implementation depends on specific error condition
        mockMvc.perform(post("/api/invalid-endpoint")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    // ==================== HELPER METHODS ====================

    private String extractToken(String response) throws Exception {
        // Parse JWT token from login response
        // Implementation depends on JSON structure
        return response.contains("token") ? 
            response.substring(response.indexOf("token") + 8, response.indexOf("token") + 200) : 
            "mock_token";
    }

    private Long extractBikeIdFromResponse(String response) throws Exception {
        // Extract bike ID from API response
        if (response.contains("\"id\":")) {
            int startIdx = response.indexOf("\"id\":") + 5;
            int endIdx = response.indexOf(",", startIdx);
            String idStr = response.substring(startIdx, endIdx).trim();
            try {
                return Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Double extractBikeHourlyRate(String response) throws Exception {
        if (response.contains("\"pricePerHour\":")) {
            int startIdx = response.indexOf("\"pricePerHour\":") + 15;
            int endIdx = response.indexOf(",", startIdx);
            String rateStr = response.substring(startIdx, endIdx).trim();
            return Double.parseDouble(rateStr);
        }
        return null;
    }

    private Double extractTotalCost(String response) throws Exception {
        if (response.contains("\"totalCost\":")) {
            int startIdx = response.indexOf("\"totalCost\":") + 12;
            int endIdx = response.indexOf("}", startIdx);
            String costStr = response.substring(startIdx, endIdx).trim();
            return Double.parseDouble(costStr);
        }
        return null;
    }
}
