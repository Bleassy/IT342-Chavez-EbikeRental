package com.ebike.rental.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ebike.rental.dto.LoginRequest;
import com.ebike.rental.dto.RegisterRequest;
import com.ebike.rental.dto.UserDTO;
import com.ebike.rental.entity.User;
import com.ebike.rental.repository.UserRepository;
import com.ebike.rental.security.JwtTokenProvider;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public User registerUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypted password
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());

        User.UserRole selectedRole = User.UserRole.USER;
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                selectedRole = User.UserRole.valueOf(request.getRole().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                selectedRole = User.UserRole.USER;
            }
        }
        user.setRole(selectedRole);

        user.setIsActive(true);
        return userRepository.save(user);
    }

    public Optional<User> loginUser(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Compare with encrypted password
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                if (request.getRole() != null && !request.getRole().isBlank()) {
                    try {
                        User.UserRole requestedRole = User.UserRole.valueOf(request.getRole().trim().toUpperCase(Locale.ROOT));
                        if (user.getRole() != requestedRole) {
                            return Optional.empty();
                        }
                    } catch (IllegalArgumentException ignored) {
                        return Optional.empty();
                    }
                }
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
    }

    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDTO);
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(this::convertToDTO);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getActiveUsers() {
        return userRepository.findByIsActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getFirstName() != null) user.setFirstName(userDetails.getFirstName());
            if (userDetails.getLastName() != null) user.setLastName(userDetails.getLastName());
            if (userDetails.getPhone() != null) user.setPhone(userDetails.getPhone());
            if (userDetails.getAddress() != null) user.setAddress(userDetails.getAddress());
            if (userDetails.getNickname() != null) user.setNickname(userDetails.getNickname());
            if (userDetails.getProfilePictureUrl() != null) user.setProfilePictureUrl(userDetails.getProfilePictureUrl());
            return userRepository.save(user);
        }).orElse(null);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getAddress(),
                user.getNickname(),
                user.getProfilePictureUrl(),
                user.getRole().toString(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }
}
