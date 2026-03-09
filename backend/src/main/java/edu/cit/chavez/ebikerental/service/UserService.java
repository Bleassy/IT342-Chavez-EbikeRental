package edu.cit.chavez.ebikerental.service;

import edu.cit.chavez.ebikerental.dto.LoginRequest;
import edu.cit.chavez.ebikerental.dto.RegisterRequest;
import edu.cit.chavez.ebikerental.entity.User;
import edu.cit.chavez.ebikerental.repository.UserRepository;
import edu.cit.chavez.ebikerental.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Register a new user.
     * Password is hashed with BCrypt before saving to the database.
     */
    public User registerUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt hash
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setRole(User.UserRole.USER);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    /**
     * Validate login credentials.
     * Compares the plain-text password against the stored BCrypt hash.
     */
    public Optional<User> loginUser(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /** Generate a signed JWT for the given user. */
    public String generateToken(User user) {
        return jwtTokenProvider.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
    }

    /** Check if an email is already registered. */
    public boolean userExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
