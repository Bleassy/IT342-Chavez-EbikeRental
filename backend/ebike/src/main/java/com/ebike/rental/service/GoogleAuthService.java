package com.ebike.rental.service;

import com.ebike.rental.entity.User;
import com.ebike.rental.dto.GoogleAuthRequest;
import com.ebike.rental.dto.AuthResponse;
import com.ebike.rental.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GoogleAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse authenticateWithGoogle(GoogleAuthRequest request) throws Exception {
        JsonNode payload;

        if (request.getCode() != null && !request.getCode().isEmpty()) {
            // Authorization code flow: exchange code for tokens
            payload = exchangeCodeForUserInfo(request.getCode(), request.getRedirectUri());
        } else if (request.getIdToken() != null && !request.getIdToken().isEmpty()) {
            // ID token flow (legacy)
            payload = parseGoogleToken(request.getIdToken());
        } else {
            throw new Exception("No authorization code or ID token provided");
        }

        if (payload == null) {
            throw new Exception("Invalid Google token or code");
        }

        String email = payload.get("email").asText();
        String firstName = payload.has("given_name") ? payload.get("given_name").asText() : "";
        String lastName = payload.has("family_name") ? payload.get("family_name").asText() : "";

        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if ((user.getFirstName() == null || user.getFirstName().isEmpty()) && !firstName.isEmpty()) {
                user.setFirstName(firstName);
            }
            if ((user.getLastName() == null || user.getLastName().isEmpty()) && !lastName.isEmpty()) {
                user.setLastName(lastName);
            }
            userRepository.save(user);
        } else {
            user = new User();
            user.setEmail(email);
            user.setFirstName(firstName.isEmpty() ? "Google" : firstName);
            user.setLastName(lastName.isEmpty() ? "User" : lastName);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Hashed random password for OAuth users
            user.setRole(User.UserRole.USER);
            user.setIsActive(true);
            user = userRepository.save(user);
        }

        String jwtToken = userService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                jwtToken,
                user.getRole().toString()
        );
    }

    private JsonNode exchangeCodeForUserInfo(String code, String redirectUri) {
        try {
            // Exchange authorization code for tokens
            String tokenRequestBody = String.format(
                    "code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                    java.net.URLEncoder.encode(code, "UTF-8"),
                    java.net.URLEncoder.encode(googleClientId, "UTF-8"),
                    java.net.URLEncoder.encode(googleClientSecret, "UTF-8"),
                    java.net.URLEncoder.encode(redirectUri, "UTF-8")
            );

            HttpRequest tokenRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                    .build();

            HttpResponse<String> tokenResponse = httpClient.send(tokenRequest, HttpResponse.BodyHandlers.ofString());

            if (tokenResponse.statusCode() != 200) {
                System.err.println("Google token exchange failed: " + tokenResponse.body());
                return null;
            }

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.body());
            String idToken = tokenJson.has("id_token") ? tokenJson.get("id_token").asText() : null;

            if (idToken != null) {
                return parseGoogleToken(idToken);
            }

            // Fallback: use access token to get user info
            String accessToken = tokenJson.get("access_token").asText();
            HttpRequest userInfoRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> userInfoResponse = httpClient.send(userInfoRequest, HttpResponse.BodyHandlers.ofString());

            if (userInfoResponse.statusCode() != 200) {
                System.err.println("Google userinfo request failed: " + userInfoResponse.body());
                return null;
            }

            return objectMapper.readTree(userInfoResponse.body());
        } catch (Exception e) {
            System.err.println("Error exchanging Google auth code: " + e.getMessage());
            return null;
        }
    }

    private JsonNode parseGoogleToken(String idTokenString) {
        try {
            String[] parts = idTokenString.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            System.err.println("Error parsing Google token: " + e.getMessage());
            return null;
        }
    }
}
