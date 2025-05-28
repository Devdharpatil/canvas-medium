package com.canvamedium.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AuthManager class
 */
public class AuthManagerTest {

    private static final String TEST_TOKEN = "test_jwt_token";
    private static final String TEST_REFRESH_TOKEN = "test_refresh_token";
    private static final long TEST_EXPIRY = System.currentTimeMillis() + 3600000; // 1 hour from now

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    private AuthManager authManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configure mocks
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putLong(anyString(), anyLong())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);
        
        // Retrieve token setup
        when(mockSharedPreferences.getString(eq("auth_token"), eq(null))).thenReturn(TEST_TOKEN);
        when(mockSharedPreferences.getString(eq("refresh_token"), eq(null))).thenReturn(TEST_REFRESH_TOKEN);
        when(mockSharedPreferences.getLong(eq("token_expiry"), eq(0L))).thenReturn(TEST_EXPIRY);
        
        // Create AuthManager instance
        authManager = AuthManager.getInstance(mockContext);
    }

    @Test
    public void testSaveToken() {
        // Arrange
        String token = "new_token";
        String refreshToken = "new_refresh_token";
        long expiry = System.currentTimeMillis() + 7200000; // 2 hours from now
        
        // Act
        authManager.saveToken(token, refreshToken, expiry);
        
        // Assert
        verify(mockEditor).putString(eq("auth_token"), eq(token));
        verify(mockEditor).putString(eq("refresh_token"), eq(refreshToken));
        verify(mockEditor).putLong(eq("token_expiry"), eq(expiry));
        verify(mockEditor).apply();
    }
    
    @Test
    public void testGetToken() {
        // Act
        String token = authManager.getToken();
        
        // Assert
        assertEquals(TEST_TOKEN, token);
    }
    
    @Test
    public void testGetRefreshToken() {
        // Act
        String refreshToken = authManager.getRefreshToken();
        
        // Assert
        assertEquals(TEST_REFRESH_TOKEN, refreshToken);
    }
    
    @Test
    public void testIsLoggedIn_WithValidToken() {
        // Arrange
        when(mockSharedPreferences.getLong(eq("token_expiry"), eq(0L)))
                .thenReturn(System.currentTimeMillis() + 3600000); // 1 hour from now
        
        // Act
        boolean isLoggedIn = authManager.isLoggedIn();
        
        // Assert
        assertTrue(isLoggedIn);
    }
    
    @Test
    public void testIsLoggedIn_WithExpiredToken() {
        // Arrange
        when(mockSharedPreferences.getLong(eq("token_expiry"), eq(0L)))
                .thenReturn(System.currentTimeMillis() - 3600000); // 1 hour ago
        
        // Act
        boolean isLoggedIn = authManager.isLoggedIn();
        
        // Assert
        assertFalse(isLoggedIn);
    }
    
    @Test
    public void testIsLoggedIn_WithNoToken() {
        // Arrange
        when(mockSharedPreferences.getString(eq("auth_token"), eq(null))).thenReturn(null);
        
        // Act
        boolean isLoggedIn = authManager.isLoggedIn();
        
        // Assert
        assertFalse(isLoggedIn);
    }
    
    @Test
    public void testLogout() {
        // Act
        authManager.logout();
        
        // Assert
        verify(mockEditor).clear();
        verify(mockEditor).apply();
    }
    
    @Test
    public void testIsTokenExpired_WithExpiredToken() {
        // Arrange
        when(mockSharedPreferences.getLong(eq("token_expiry"), eq(0L)))
                .thenReturn(System.currentTimeMillis() - 3600000); // 1 hour ago
        
        // Act
        boolean isExpired = authManager.isTokenExpired();
        
        // Assert
        assertTrue(isExpired);
    }
    
    @Test
    public void testIsTokenExpired_WithValidToken() {
        // Arrange
        when(mockSharedPreferences.getLong(eq("token_expiry"), eq(0L)))
                .thenReturn(System.currentTimeMillis() + 3600000); // 1 hour from now
        
        // Act
        boolean isExpired = authManager.isTokenExpired();
        
        // Assert
        assertFalse(isExpired);
    }
} 