package com.canvamedium.controller;

import com.canvamedium.model.User;
import com.canvamedium.security.JwtUtils;
import com.canvamedium.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication requests.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
public class AuthController {
    
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Constructor with dependencies injection.
     *
     * @param userService           The user service
     * @param jwtUtils              The JWT utility
     * @param authenticationManager The authentication manager
     */
    @Autowired
    public AuthController(UserService userService, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }
    
    /**
     * Authenticate a user and generate JWT token.
     *
     * @param loginRequest The login request
     * @return The response entity with authentication token and user details
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticate user and generate JWT token")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(loginRequest.getUsername());
            
            // Record login
            userService.recordLogin(loginRequest.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("refreshToken", refreshToken);
            
            // Add user details to response
            userService.findByUsername(loginRequest.getUsername()).ifPresent(user -> {
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("id", user.getId());
                userDetails.put("username", user.getUsername());
                userDetails.put("email", user.getEmail());
                userDetails.put("fullName", user.getFullName());
                
                response.put("user", userDetails);
            });
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password"));
        }
    }
    
    /**
     * Register a new user.
     *
     * @param registerRequest The registration request
     * @return The response entity with the user details
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if username is available
            if (!userService.isUsernameAvailable(registerRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username is already taken", "field", "username"));
            }
            
            // Check if email is available
            if (!userService.isEmailAvailable(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email is already in use", "field", "email"));
            }
            
            // Create new user
            User user = new User(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getFullName()
            );
            
            User registeredUser = userService.registerUser(user);
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }
    
    /**
     * Refresh JWT token.
     *
     * @param refreshRequest The refresh token request
     * @return The response entity with the new tokens
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get a new access token using a refresh token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        
        if (!jwtUtils.validateJwtToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid refresh token"));
        }
        
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        
        try {
            UserDetails userDetails = userService.loadUserByUsername(username);
            
            // Generate new tokens
            String newToken = jwtUtils.generateJwtToken(username, userDetails.getAuthorities());
            String newRefreshToken = jwtUtils.generateRefreshToken(username);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", newToken);
            response.put("refreshToken", newRefreshToken);
            
            return ResponseEntity.ok(response);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }
    }
    
    /**
     * Check if a username is available.
     *
     * @param username The username to check
     * @return The response entity indicating if the username is available
     */
    @GetMapping("/check-username/{username}")
    @Operation(summary = "Check username availability", description = "Check if a username is available for registration")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
    
    /**
     * Check if an email is available.
     *
     * @param email The email to check
     * @return The response entity indicating if the email is available
     */
    @GetMapping("/check-email/{email}")
    @Operation(summary = "Check email availability", description = "Check if an email is available for registration")
    public ResponseEntity<?> checkEmailAvailability(@PathVariable String email) {
        boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
    
    /**
     * DTO for login requests.
     */
    public static class LoginRequest {
        private String username;
        private String password;
        
        // Getters and setters
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    /**
     * DTO for registration requests.
     */
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String fullName;
        
        // Getters and setters
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
    
    /**
     * DTO for refresh token requests.
     */
    public static class RefreshTokenRequest {
        private String refreshToken;
        
        // Getters and setters
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
} 