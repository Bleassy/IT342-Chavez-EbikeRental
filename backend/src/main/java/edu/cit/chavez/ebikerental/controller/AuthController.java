package edu.cit.chavez.ebikerental.controller;

import edu.cit.chavez.ebikerental.dto.ApiResponse;
import edu.cit.chavez.ebikerental.dto.AuthResponse;
import edu.cit.chavez.ebikerental.dto.LoginRequest;
import edu.cit.chavez.ebikerental.dto.RegisterRequest;
import edu.cit.chavez.ebikerental.entity.User;
import edu.cit.chavez.ebikerental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * POST /api/auth/register
     * Registers a new user account.
     *
     * Request body: { email, password, firstName, lastName, phone, address }
     * Returns 201 on success, 409 if email already exists.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        try {
            // Validate required fields
            if (request.getEmail() == null || request.getEmail().isBlank()
                    || request.getPassword() == null || request.getPassword().isBlank()
                    || request.getFirstName() == null || request.getFirstName().isBlank()
                    || request.getLastName() == null || request.getLastName().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Name, email, and password are required"));
            }

            // Prevent duplicate email registration
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

    /**
     * POST /api/auth/login
     * Authenticates an existing user.
     *
     * Request body: { email, password }
     * Returns 200 + JWT on success, 401 on invalid credentials.
     */
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
                        .body(new ApiResponse<>(false, "Invalid email or password"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Login failed: " + e.getMessage()));
        }
    }
}
