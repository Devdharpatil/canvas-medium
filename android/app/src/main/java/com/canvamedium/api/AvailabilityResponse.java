package com.canvamedium.api;

import com.google.gson.annotations.SerializedName;

/**
 * Response class for username and email availability checks.
 */
public class AvailabilityResponse {
    
    @SerializedName("available")
    private boolean available;
    
    @SerializedName("message")
    private String message;
    
    /**
     * Default constructor.
     */
    public AvailabilityResponse() {
    }
    
    /**
     * Constructor with availability and message.
     *
     * @param available Whether the username/email is available
     * @param message A message explaining the result
     */
    public AvailabilityResponse(boolean available, String message) {
        this.available = available;
        this.message = message;
    }
    
    /**
     * Gets whether the username/email is available.
     *
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Sets whether the username/email is available.
     *
     * @param available true if available, false otherwise
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    /**
     * Gets the message explaining the result.
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the message explaining the result.
     *
     * @param message The message
     */
    public void setMessage(String message) {
        this.message = message;
    }
} 