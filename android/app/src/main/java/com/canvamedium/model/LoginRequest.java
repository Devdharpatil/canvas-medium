package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model class representing the login request payload.
 */
public class LoginRequest {

    @SerializedName("username")
    private String email;

    @SerializedName("password")
    private String password;

    /**
     * Constructor for login request.
     *
     * @param email     The user's email address
     * @param password  The user's password
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Gets the email.
     *
     * @return The email used for authentication
     */
    public String getUsername() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email The email to set for authentication
     */
    public void setUsername(String email) {
        this.email = email;
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