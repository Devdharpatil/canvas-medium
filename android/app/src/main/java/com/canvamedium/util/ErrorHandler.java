package com.canvamedium.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.canvamedium.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Centralized error handling utility for the application.
 * Provides methods for handling different types of errors in a consistent way.
 */
public class ErrorHandler {

    private static final String TAG = "ErrorHandler";
    
    // Error types
    public enum ErrorType {
        NETWORK,
        SERVER,
        AUTH,
        CLIENT,
        VALIDATION,
        UNKNOWN
    }
    
    /**
     * Interface for handling errors
     */
    public interface ErrorCallback {
        void onError(ErrorType type, String message, Throwable error);
    }
    
    private static Map<Integer, ErrorType> errorCodes;
    
    static {
        errorCodes = new HashMap<>();
        // Client errors
        errorCodes.put(400, ErrorType.CLIENT);       // Bad Request
        errorCodes.put(401, ErrorType.AUTH);         // Unauthorized
        errorCodes.put(403, ErrorType.AUTH);         // Forbidden
        errorCodes.put(404, ErrorType.CLIENT);       // Not Found
        errorCodes.put(409, ErrorType.VALIDATION);   // Conflict
        errorCodes.put(422, ErrorType.VALIDATION);   // Unprocessable Entity
        
        // Server errors
        for (int i = 500; i < 600; i++) {
            errorCodes.put(i, ErrorType.SERVER);
        }
    }
    
    /**
     * Handle a Retrofit Response that contains an error.
     *
     * @param response The Retrofit Response
     * @return An error message
     */
    public static String handleErrorResponse(@NonNull Response<?> response) {
        ErrorType errorType = getErrorTypeFromCode(response.code());
        
        String errorMessage = getErrorMessageFromResponse(response);
        logError(errorType, errorMessage, null);
        
        return errorMessage;
    }
    
    /**
     * Handle a general network exception.
     *
     * @param throwable The network exception
     * @return An error message
     */
    public static String handleNetworkException(@NonNull Throwable throwable) {
        ErrorType errorType = ErrorType.NETWORK;
        String errorMessage;
        
        if (throwable instanceof ConnectException || throwable instanceof UnknownHostException) {
            errorMessage = "Cannot connect to server. Please check your internet connection.";
        } else if (throwable instanceof SocketTimeoutException) {
            errorMessage = "Connection timed out. Please try again.";
        } else if (throwable instanceof IOException) {
            errorMessage = "Network error. Please check your internet connection and try again.";
        } else {
            errorType = ErrorType.UNKNOWN;
            errorMessage = "An unexpected error occurred. Please try again.";
        }
        
        logError(errorType, errorMessage, throwable);
        return errorMessage;
    }
    
    /**
     * Display an error message as a Toast.
     *
     * @param context The context
     * @param message The error message
     */
    public static void showToast(@NonNull Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Display an error message as a Snackbar.
     *
     * @param view The view to show the Snackbar on
     * @param message The error message
     * @return The Snackbar instance
     */
    public static Snackbar showSnackbar(@NonNull android.view.View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
        return snackbar;
    }
    
    /**
     * Display an error message as a Snackbar with an action.
     *
     * @param view The view to show the Snackbar on
     * @param message The error message
     * @param actionText The text for the action button
     * @param action The action to perform when the button is clicked
     * @return The Snackbar instance
     */
    public static Snackbar showSnackbarWithAction(
            @NonNull android.view.View view,
            String message,
            String actionText,
            android.view.View.OnClickListener action) {
        
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(actionText, action);
        snackbar.show();
        return snackbar;
    }
    
    /**
     * Extract an error message from a Retrofit Response.
     *
     * @param response The Retrofit Response
     * @return The error message
     */
    private static String getErrorMessageFromResponse(@NonNull Response<?> response) {
        try {
            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                String errorString = errorBody.string();
                
                try {
                    JsonObject jsonObject = JsonParser.parseString(errorString).getAsJsonObject();
                    
                    // Try common error message fields
                    String[] possibleFields = {"message", "error", "error_message", "errorMessage"};
                    for (String field : possibleFields) {
                        if (jsonObject.has(field)) {
                            return jsonObject.get(field).getAsString();
                        }
                    }
                    
                    // If we can't find a specific error field, return the whole JSON
                    return errorString;
                    
                } catch (JsonSyntaxException e) {
                    // If it's not JSON, return the raw string
                    return errorString;
                }
            }
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "Error parsing error response", e);
        }
        
        // If all else fails, return a generic error based on status code
        return getGenericErrorMessage(response.code());
    }
    
    /**
     * Get the error type from an HTTP status code.
     *
     * @param statusCode The HTTP status code
     * @return The error type
     */
    private static ErrorType getErrorTypeFromCode(int statusCode) {
        return errorCodes.getOrDefault(statusCode, ErrorType.UNKNOWN);
    }
    
    /**
     * Get a generic error message based on an HTTP status code.
     *
     * @param statusCode The HTTP status code
     * @return A generic error message
     */
    private static String getGenericErrorMessage(int statusCode) {
        if (statusCode == 401 || statusCode == 403) {
            return "Authentication error. Please log in again.";
        } else if (statusCode == 404) {
            return "The requested resource was not found.";
        } else if (statusCode == 409 || statusCode == 422) {
            return "The request could not be processed due to validation errors.";
        } else if (statusCode >= 500) {
            return "A server error occurred. Please try again later.";
        } else {
            return "Error: " + statusCode;
        }
    }
    
    /**
     * Log an error.
     *
     * @param type The error type
     * @param message The error message
     * @param throwable The exception, or null if not available
     */
    private static void logError(ErrorType type, String message, Throwable throwable) {
        String errorTag = TAG + "_" + type.name();
        if (throwable != null) {
            Log.e(errorTag, message, throwable);
        } else {
            Log.e(errorTag, message);
        }
    }
} 