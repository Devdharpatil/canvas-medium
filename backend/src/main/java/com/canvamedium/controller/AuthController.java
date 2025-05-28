package com.canvamedium.controller;

import com.canvamedium.exception.ErrorResponse;
import com.canvamedium.exception.SuccessResponse;
import com.canvamedium.model.User;
import com.canvamedium.security.JwtUtils;
import com.canvamedium.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
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
            
            Map<String, Object> userData = new HashMap<>();
            
            // Add user details to response
            userService.findByUsername(loginRequest.getUsername()).ifPresent(user -> {
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("email", user.getEmail());
                userData.put("fullName", user.getFullName());
            });
            
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("token", jwt);
            tokenData.put("refreshToken", refreshToken);
            
            return ResponseEntity.ok(new SuccessResponse("Login successful", Map.of(
                "user", userData,
                "auth", tokenData
            )));
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for user {}: Bad credentials", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "Invalid username or password"));
        } catch (Exception e) {
            logger.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "An error occurred during login"));
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
                logger.warn("Registration failed: Username {} is already taken", registerRequest.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Username is already taken"));
            }
            
            // Check if email is available
            if (!userService.isEmailAvailable(registerRequest.getEmail())) {
                logger.warn("Registration failed: Email {} is already in use", registerRequest.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Email is already in use"));
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
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", registeredUser.getId());
            userData.put("username", registeredUser.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse(201, "User registered successfully", userData));
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Registration failed: " + e.getMessage()));
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
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            
            if (!jwtUtils.validateJwtToken(refreshToken)) {
                logger.warn("Token refresh failed: Invalid refresh token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Invalid refresh token"));
            }
            
            String username = jwtUtils.getUsernameFromToken(refreshToken);
            
            UserDetails userDetails = userService.loadUserByUsername(username);
            
            // Generate new tokens
            String newToken = jwtUtils.generateJwtToken(username, userDetails.getAuthorities());
            String newRefreshToken = jwtUtils.generateRefreshToken(username);
            
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("token", newToken);
            tokenData.put("refreshToken", newRefreshToken);
            
            return ResponseEntity.ok(new SuccessResponse("Token refreshed successfully", tokenData));
        } catch (UsernameNotFoundException e) {
            logger.warn("Token refresh failed: User not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "User not found"));
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "An error occurred during token refresh"));
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
        try {
            boolean isAvailable = userService.isUsernameAvailable(username);
            return ResponseEntity.ok(new SuccessResponse("Username availability checked", Map.of("available", isAvailable)));
        } catch (Exception e) {
            logger.error("Username availability check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Failed to check username availability"));
        }
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
        try {
            boolean isAvailable = userService.isEmailAvailable(email);
            return ResponseEntity.ok(new SuccessResponse("Email availability checked", Map.of("available", isAvailable)));
        } catch (Exception e) {
            logger.error("Email availability check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Failed to check email availability"));
        }
    }
    
    /**
     * DTO for login requests.
     */
    public static class LoginRequest {
        private String username; // Can be either username or email
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