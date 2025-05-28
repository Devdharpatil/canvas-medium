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

    @SerializedName("userId")
    private Long userId;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

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
     * @return The user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email.
     *
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email The email
     */
    public void setEmail(String email) {
        this.email = email;
    }
} 