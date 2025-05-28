package com.canvamedium.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API error response for validation errors, with field-specific error details.
 */
public class ValidationError extends ApiError {
    
    private Map<String, String> errors;
    
    /**
     * Constructor with all fields.
     *
     * @param status    HTTP status code
     * @param message   Error message
     * @param timestamp Timestamp when the error occurred
     * @param errors    Map of field-specific validation errors
     */
    public ValidationError(int status, String message, LocalDateTime timestamp, Map<String, String> errors) {
        super(status, message, timestamp);
        this.errors = errors;
    }
    
    /**
     * Get the field-specific validation errors.
     *
     * @return Map of field-specific validation errors
     */
    public Map<String, String> getErrors() {
        return errors;
    }
    
    /**
     * Set the field-specific validation errors.
     *
     * @param errors Map of field-specific validation errors
     */
    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
} 