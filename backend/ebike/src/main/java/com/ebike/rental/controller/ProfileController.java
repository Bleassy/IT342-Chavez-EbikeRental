package com.ebike.rental.controller;

import com.ebike.rental.entity.User;
import com.ebike.rental.dto.UserDTO;
import com.ebike.rental.dto.ApiResponse;
import com.ebike.rental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileController {

    @Autowired
    private UserService userService;

    /**
     * Get the currently authenticated user's profile.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserDTO>> getProfile() {
        try {
            String email = getCurrentUserEmail();
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Not authenticated"));
            }
            Optional<UserDTO> user = userService.getUserByEmail(email);
            if (user.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Profile retrieved successfully", user.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve profile: " + e.getMessage()));
        }
    }

    /**
     * Update the currently authenticated user's profile.
     * Only allows updating: firstName, lastName, phone, address, nickname, profilePictureUrl.
     */
    @PutMapping
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@RequestBody User userDetails) {
        try {
            String email = getCurrentUserEmail();
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Not authenticated"));
            }
            Optional<UserDTO> existing = userService.getUserByEmail(email);
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }

            Long userId = existing.get().getId();
            User updated = userService.updateUser(userId, userDetails);
            if (updated != null) {
                UserDTO dto = new UserDTO(
                        updated.getId(),
                        updated.getEmail(),
                        updated.getFirstName(),
                        updated.getLastName(),
                        updated.getPhone(),
                        updated.getAddress(),
                        updated.getNickname(),
                        updated.getProfilePictureUrl(),
                        updated.getRole().toString(),
                        updated.getIsActive(),
                        updated.getCreatedAt()
                );
                return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update profile: " + e.getMessage()));
        }
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName(); // This is the email set as the principal in JwtAuthenticationFilter
        }
        return null;
    }
}
