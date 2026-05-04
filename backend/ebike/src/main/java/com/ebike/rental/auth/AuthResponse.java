package com.ebike.rental.auth;

public class AuthResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String token;
    private String role;

    public AuthResponse(Long id, String email, String firstName, String lastName, String token, String role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
