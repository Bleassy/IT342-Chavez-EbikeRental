package com.ebike.rental.controller;

import com.ebike.rental.entity.User;
import com.ebike.rental.dto.UserDTO;
import com.ebike.rental.dto.AuthResponse;
import com.ebike.rental.dto.LoginRequest;
import com.ebike.rental.dto.RegisterRequest;
import com.ebike.rental.dto.GoogleAuthRequest;
import com.ebike.rental.dto.ApiResponse;
import com.ebike.rental.service.UserService;
import com.ebike.rental.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        try {
            if (userService.userExists(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, "Email already registered"));
            }

            User user = userService.registerUser(request);
            String token = userService.generateToken(user);

            AuthResponse authResponse = new AuthResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    token,
                    user.getRole().toString()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "User registered successfully", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> user = userService.loginUser(request);

            if (user.isPresent()) {
                User loggedInUser = user.get();
                String token = userService.generateToken(loggedInUser);

                AuthResponse authResponse = new AuthResponse(
                        loggedInUser.getId(),
                        loggedInUser.getEmail(),
                        loggedInUser.getFirstName(),
                        loggedInUser.getLastName(),
                        token,
                        loggedInUser.getRole().toString()
                );

                return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", authResponse));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Invalid credentials"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/oauth2/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(@RequestBody GoogleAuthRequest request) {
        try {
            AuthResponse authResponse = googleAuthService.authenticateWithGoogle(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Google login successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Google authentication failed: " + e.getMessage()));
        }
    }
}

