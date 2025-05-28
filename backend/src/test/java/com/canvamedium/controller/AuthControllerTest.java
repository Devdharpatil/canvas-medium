package com.canvamedium.controller;

import com.canvamedium.model.User;
import com.canvamedium.security.JwtUtils;
import com.canvamedium.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the AuthController class.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for testing
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthenticationManager authenticationManager;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        testUser = new User("testuser", "test@example.com", "password", "Test User");
        testUser.setId(1L);
    }

    @Test
    void registerUser_WithValidData_ShouldReturnSuccess() throws Exception {
        // Arrange
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");

        when(userService.isUsernameAvailable("newuser")).thenReturn(true);
        when(userService.isEmailAvailable("new@example.com")).thenReturn(true);
        when(userService.registerUser(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("User registered successfully")))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("testuser")));
        
        verify(userService).registerUser(any(User.class));
    }

    @Test
    void registerUser_WithExistingUsername_ShouldReturnBadRequest() throws Exception {
        // Arrange
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername("existinguser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");

        when(userService.isUsernameAvailable("existinguser")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username is already taken")));
        
        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        AuthController.RegisterRequest registerRequest = new AuthController.RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");

        when(userService.isUsernameAvailable("newuser")).thenReturn(true);
        when(userService.isEmailAvailable("existing@example.com")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Email is already in use")));
        
        verify(userService, never()).registerUser(any(User.class));
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewTokens() throws Exception {
        // Arrange
        AuthController.RefreshTokenRequest refreshRequest = new AuthController.RefreshTokenRequest();
        refreshRequest.setRefreshToken("valid_refresh_token");

        when(jwtUtils.validateJwtToken("valid_refresh_token")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("valid_refresh_token")).thenReturn("testuser");
        
        UserDetails userDetails = mock(UserDetails.class);
        when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);
        
        when(jwtUtils.generateJwtToken(eq("testuser"), any())).thenReturn("new_jwt_token");
        when(jwtUtils.generateRefreshToken("testuser")).thenReturn("new_refresh_token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("new_jwt_token")))
                .andExpect(jsonPath("$.refreshToken", is("new_refresh_token")));
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        AuthController.RefreshTokenRequest refreshRequest = new AuthController.RefreshTokenRequest();
        refreshRequest.setRefreshToken("invalid_refresh_token");

        when(jwtUtils.validateJwtToken("invalid_refresh_token")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid refresh token")));
    }

    @Test
    void refreshToken_WithNonExistingUser_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        AuthController.RefreshTokenRequest refreshRequest = new AuthController.RefreshTokenRequest();
        refreshRequest.setRefreshToken("valid_refresh_token");

        when(jwtUtils.validateJwtToken("valid_refresh_token")).thenReturn(true);
        when(jwtUtils.getUsernameFromToken("valid_refresh_token")).thenReturn("nonexistent");
        
        when(userService.loadUserByUsername("nonexistent"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    @WithMockUser
    void checkUsernameAvailability_WithAvailableUsername_ShouldReturnTrue() throws Exception {
        // Arrange
        when(userService.isUsernameAvailable("available")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/auth/check-username/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    @WithMockUser
    void checkUsernameAvailability_WithUnavailableUsername_ShouldReturnFalse() throws Exception {
        // Arrange
        when(userService.isUsernameAvailable("taken")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/check-username/taken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    @WithMockUser
    void checkEmailAvailability_WithAvailableEmail_ShouldReturnTrue() throws Exception {
        // Arrange
        when(userService.isEmailAvailable("available@example.com")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/auth/check-email/available@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    @WithMockUser
    void checkEmailAvailability_WithUnavailableEmail_ShouldReturnFalse() throws Exception {
        // Arrange
        when(userService.isEmailAvailable("taken@example.com")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/auth/check-email/taken@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available", is(false)));
    }
} 