package com.canvamedium.exception;

import java.time.LocalDateTime;

/**
 * Standard API error response.
 */
public class ApiError {
    
    private int status;
    private String message;
    private LocalDateTime timestamp;
    
    /**
     * Constructor with all fields.
     *
     * @param status    HTTP status code
     * @param message   Error message
     * @param timestamp Timestamp when the error occurred
     */
    public ApiError(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    /**
     * Get the HTTP status code.
     *
     * @return HTTP status code
     */
    public int getStatus() {
        return status;
    }
    
    /**
     * Set the HTTP status code.
     *
     * @param status HTTP status code
     */
    public void setStatus(int status) {
        this.status = status;
    }
    
    /**
     * Get the error message.
     *
     * @return Error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Set the error message.
     *
     * @param message Error message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Get the timestamp when the error occurred.
     *
     * @return Timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the timestamp when the error occurred.
     *
     * @param timestamp Timestamp
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
} 