package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model class representing the login request payload.
 */
public class LoginRequest {

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    /**
     * Constructor for login request.
     *
     * @param username  The user's username or email
     * @param password  The user's password
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
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
     * @param username The username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password The password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
} 