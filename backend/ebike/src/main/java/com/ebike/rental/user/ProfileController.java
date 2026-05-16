package com.ebike.rental.user;

import com.ebike.rental.dto.ApiResponse;

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

    /**
     * Upload or update user's profile picture (Base64 encoded).
     * Accepts a JSON request with "profilePic" field containing Base64 encoded image.
     */
    @PostMapping("/pic")
    public ResponseEntity<ApiResponse<UserDTO>> uploadProfilePicture(@RequestBody java.util.Map<String, String> request) {
        try {
            String email = getCurrentUserEmail();
            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Not authenticated"));
            }

            String base64Image = request.get("profilePic");
            if (base64Image == null || base64Image.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Profile picture data is required"));
            }

            // Validate Base64 format (basic check)
            if (!isValidBase64(base64Image)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Invalid image format"));
            }

            Optional<UserDTO> existing = userService.getUserByEmail(email);
            if (existing.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }

            Long userId = existing.get().getId();
            User userDetails = new User();
            userDetails.setProfilePictureUrl(base64Image);

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
                return ResponseEntity.ok(new ApiResponse<>(true, "Profile picture uploaded successfully", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to upload profile picture: " + e.getMessage()));
        }
    }

    /**
     * Basic validation for Base64 encoded string
     */
    private boolean isValidBase64(String base64String) {
        try {
            return base64String != null && !base64String.isEmpty() && 
                   base64String.matches("^[A-Za-z0-9+/=]*$");
        } catch (Exception e) {
            return false;
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
