package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for refreshing authentication token.
 */
public class RefreshTokenRequest {
    
    @SerializedName("refresh_token")
    private String refreshToken;
    
    /**
     * Default constructor.
     */
    public RefreshTokenRequest() {
    }
    
    /**
     * Constructor with refresh token.
     *
     * @param refreshToken the refresh token
     */
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    /**
     * Get the refresh token.
     *
     * @return the refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }
    
    /**
     * Set the refresh token.
     *
     * @param refreshToken the refresh token
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
} 