package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model class representing the login response from the API.
 */
public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("refreshToken")
    private String refreshToken;
    
    @SerializedName("expiry")
    private long expiry;

    @SerializedName("user")
    private User user;

    /**
     * Default constructor.
     */
    public LoginResponse() {
    }

    /**
     * Gets the authentication token.
     *
     * @return The JWT token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the authentication token.
     *
     * @param token The JWT token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Gets the refresh token.
     *
     * @return The refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets the refresh token.
     *
     * @param refreshToken The refresh token
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    /**
     * Gets the token expiry time in milliseconds (Unix timestamp).
     *
     * @return The token expiry time
     */
    public long getExpiry() {
        return expiry;
    }

    /**
     * Sets the token expiry time in milliseconds (Unix timestamp).
     *
     * @param expiry The token expiry time
     */
    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID or null if user is null
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * Gets the username.
     *
     * @return The username or null if user is null
     */
    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }
    
    /**
     * Gets the email.
     *
     * @return The email or null if user is null
     */
    public String getEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    /**
     * Gets the user object.
     *
     * @return The user object
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Sets the user object.
     *
     * @param user The user object
     */
    public void setUser(User user) {
        this.user = user;
    }
    
    /**
     * Nested User class to match API response structure.
     */
    public static class User {
        @SerializedName("id")
        private Long id;
        
        @SerializedName("username")
        private String username;
        
        @SerializedName("email")
        private String email;
        
        @SerializedName("fullName")
        private String fullName;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
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
        
        public String getFullName() {
            return fullName;
        }
        
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
} 