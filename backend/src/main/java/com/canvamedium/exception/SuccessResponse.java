package com.canvamedium.exception;

import java.time.LocalDateTime;

/**
 * Standard success response for REST API operations.
 */
public class SuccessResponse {
    private int status;
    private String message;
    private String timestamp;
    private Object data;

    /**
     * Default constructor.
     */
    public SuccessResponse() {
        this.status = 200;
        this.timestamp = LocalDateTime.now().toString();
    }

    /**
     * Constructor with success message.
     *
     * @param message The success message
     */
    public SuccessResponse(String message) {
        this();
        this.message = message;
    }

    /**
     * Constructor with success message and data.
     *
     * @param message The success message
     * @param data    The response data
     */
    public SuccessResponse(String message, Object data) {
        this();
        this.message = message;
        this.data = data;
    }

    /**
     * Constructor with status, success message, and data.
     *
     * @param status  The HTTP status code
     * @param message The success message
     * @param data    The response data
     */
    public SuccessResponse(int status, String message, Object data) {
        this();
        this.status = status;
        this.message = message;
        this.data = data;
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
} 