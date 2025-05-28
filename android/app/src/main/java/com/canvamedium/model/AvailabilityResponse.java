package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model class representing an availability response from the API.
 */
public class AvailabilityResponse implements Serializable {

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
     * Constructor with parameters.
     *
     * @param available Whether the requested resource is available
     * @param message The message explaining the availability status
     */
    public AvailabilityResponse(boolean available, String message) {
        this.available = available;
        this.message = message;
    }
    
    /**
     * Gets whether the resource is available.
     *
     * @return True if available, false otherwise
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Sets whether the resource is available.
     *
     * @param available The availability status
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    /**
     * Gets the message explaining the availability status.
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the message explaining the availability status.
     *
     * @param message The message
     */
    public void setMessage(String message) {
        this.message = message;
    }
} 