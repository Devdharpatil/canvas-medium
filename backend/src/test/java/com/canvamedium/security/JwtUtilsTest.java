package com.canvamedium.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the JwtUtils class.
 */
public class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private static final int TEST_EXPIRATION = 60000; // 1 minute
    private static final int TEST_REFRESH_EXPIRATION = 120000; // 2 minutes
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", key);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtils, "jwtRefreshExpirationMs", TEST_REFRESH_EXPIRATION);
    }

    @Test
    void generateJwtToken_WithAuthentication_ShouldReturnValidToken() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        Collection<SimpleGrantedAuthority> authorities = 
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(authorities)
                .build();
        
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        String token = jwtUtils.generateJwtToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("testuser", jwtUtils.getUsernameFromToken(token));
        assertEquals(1, jwtUtils.getRolesFromToken(token).size());
        assertTrue(jwtUtils.getRolesFromToken(token).contains("ROLE_USER"));
    }

    @Test
    void generateJwtToken_WithUsernameAndAuthorities_ShouldReturnValidToken() {
        // Arrange
        String username = "testuser";
        Collection<GrantedAuthority> authorities = 
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Act
        String token = jwtUtils.generateJwtToken(username, authorities);

        // Assert
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals(username, jwtUtils.getUsernameFromToken(token));
        assertEquals(2, jwtUtils.getRolesFromToken(token).size());
        assertTrue(jwtUtils.getRolesFromToken(token).contains("ROLE_USER"));
        assertTrue(jwtUtils.getRolesFromToken(token).contains("ROLE_ADMIN"));
    }

    @Test
    void generateRefreshToken_WithUsername_ShouldReturnValidToken() {
        // Arrange
        String username = "testuser";

        // Act
        String refreshToken = jwtUtils.generateRefreshToken(username);

        // Assert
        assertNotNull(refreshToken);
        assertTrue(jwtUtils.validateJwtToken(refreshToken));
        assertEquals(username, jwtUtils.getUsernameFromToken(refreshToken));
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String username = "testuser";
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtUtils.generateJwtToken(username, authorities);

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_WithExpiredToken_ShouldReturnFalse() throws Exception {
        // Arrange - set a very short expiration
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1);
        
        String username = "testuser";
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtUtils.generateJwtToken(username, authorities);
        
        // Wait for token to expire
        Thread.sleep(10);

        // Act
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_WithValidToken_ShouldReturnUsername() {
        // Arrange
        String username = "testuser";
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtUtils.generateJwtToken(username, authorities);

        // Act
        String extractedUsername = jwtUtils.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void getRolesFromToken_WithValidToken_ShouldReturnRoles() {
        // Arrange
        String username = "testuser";
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        String token = jwtUtils.generateJwtToken(username, authorities);

        // Act
        List<String> roles = jwtUtils.getRolesFromToken(token);

        // Assert
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void getExpirationDateFromToken_WithValidToken_ShouldReturnExpirationDate() {
        // Arrange
        String username = "testuser";
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtUtils.generateJwtToken(username, authorities);

        // Act
        Date expirationDate = jwtUtils.getExpirationDateFromToken(token);

        // Assert
        assertNotNull(expirationDate);
        
        // Expiration should be roughly the current time + TEST_EXPIRATION (with some margin for test execution)
        long expectedExpirationTime = System.currentTimeMillis() + TEST_EXPIRATION;
        long actualExpirationTime = expirationDate.getTime();
        
        // Allow for a 5-second margin due to test execution time
        assertTrue(Math.abs(expectedExpirationTime - actualExpirationTime) < 5000);
    }

    @Test
    void isTokenExpired_WithExpiredToken_ShouldReturnTrue() throws Exception {
        // Arrange - set a very short expiration
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1);
        
        String username = "testuser";
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtUtils.generateJwtToken(username, authorities);
        
        // Wait for token to expire
        Thread.sleep(10);

        // Act
        boolean isExpired = jwtUtils.isTokenExpired(token);

        // Assert
        assertTrue(isExpired);
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        // Arrange
        String username = "testuser";
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtUtils.generateJwtToken(username, authorities);

        // Act
        boolean isExpired = jwtUtils.isTokenExpired(token);

        // Assert
        assertFalse(isExpired);
    }
}
