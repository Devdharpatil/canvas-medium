package com.canvamedium.exception;

import java.time.LocalDateTime;

/**
 * Standard error response for REST API errors.
 */
public class ErrorResponse {
    private int status;
    private String message;
    private String timestamp;

    /**
     * Default constructor.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    /**
     * Constructor with error message.
     *
     * @param message The error message
     */
    public ErrorResponse(String message) {
        this();
        this.message = message;
    }

    /**
     * Constructor with status and error message.
     *
     * @param status  The HTTP status code
     * @param message The error message
     */
    public ErrorResponse(int status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    // Getters and setters

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
} 