package com.ebike.rental.controller;

import com.ebike.rental.entity.User;
import com.ebike.rental.dto.UserDTO;
import com.ebike.rental.dto.ApiResponse;
import com.ebike.rental.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve users: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getActiveUsers() {
        try {
            List<UserDTO> users = userService.getActiveUsers();
            return ResponseEntity.ok(new ApiResponse<>(true, "Active users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve active users: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        try {
            Optional<UserDTO> user = userService.getUserById(id);
            if (user.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", user.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to retrieve user: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            if (updatedUser != null) {
                UserDTO dto = new UserDTO(
                        updatedUser.getId(),
                        updatedUser.getEmail(),
                        updatedUser.getFirstName(),
                        updatedUser.getLastName(),
                        updatedUser.getPhone(),
                        updatedUser.getAddress(),
                        updatedUser.getNickname(),
                        updatedUser.getProfilePictureUrl(),
                        updatedUser.getRole().toString(),
                        updatedUser.getIsActive(),
                        updatedUser.getCreatedAt()
                );
                return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", dto));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            if (userService.deleteUser(id)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", ""));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete user: " + e.getMessage()));
        }
    }
}
