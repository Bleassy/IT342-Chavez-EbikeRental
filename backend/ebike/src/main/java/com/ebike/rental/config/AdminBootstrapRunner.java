package com.ebike.rental.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ebike.rental.entity.User;
import com.ebike.rental.repository.UserRepository;

@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.email:admin@ebike.com}")
    private String adminEmail;

    @Value("${admin.bootstrap.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Check if admin exists
        var existingAdmin = userRepository.findByEmail(adminEmail);
        
        if (existingAdmin.isEmpty()) {
            // Create default admin user
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(User.UserRole.ADMIN);
            admin.setIsActive(true);
            admin.setPhone("0000000000");
            admin.setAddress("Admin Office");
            
            userRepository.save(admin);
            System.out.println("✓ Default admin user created: " + adminEmail);
        } else {
            System.out.println("✓ Admin user already exists: " + adminEmail);
        }
    }
}
