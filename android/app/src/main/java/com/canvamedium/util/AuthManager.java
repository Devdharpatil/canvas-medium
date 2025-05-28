package com.canvamedium.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.canvamedium.api.ApiClient;
import com.canvamedium.api.AuthService;
import com.canvamedium.model.LoginResponse;
import com.canvamedium.model.RefreshTokenRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manager class for handling authentication tokens and user info.
 */
public class AuthManager {

    private static final String TAG = "AuthManager";
    private static final String PREF_FILE_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    
    // Buffer time to refresh token before it expires (5 minutes in milliseconds)
    private static final long TOKEN_REFRESH_BUFFER_MS = 5 * 60 * 1000;

    private static AuthManager instance;
    private static AuthManager testInstance = null;
    private SharedPreferences sharedPreferences;
    private Context context;
    private Executor executor;
    
    // Listener for token refresh events
    public interface TokenRefreshListener {
        void onTokenRefreshed();
        void onTokenRefreshFailed();
    }
    
    private TokenRefreshListener tokenRefreshListener;

    /**
     * Get the singleton instance of AuthManager.
     *
     * @param context The application context
     * @return The AuthManager instance
     */
    public static synchronized AuthManager getInstance(Context context) {
        // For testing purposes
        if (testInstance != null) {
            return testInstance;
        }
        
        if (instance == null) {
            instance = new AuthManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Private constructor.
     *
     * @param context The application context
     */
    private AuthManager(Context context) {
        this.context = context.getApplicationContext();
        executor = Executors.newSingleThreadExecutor();
        
        try {
            // Create or retrieve the master key for encryption
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setKeyGenParameterSpec(new KeyGenParameterSpec.Builder(
                            "_androidx_security_master_key_",
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .build())
                    .build();

            // Initialize the encrypted shared preferences
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences", e);
            // Fallback to regular SharedPreferences
            sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        }
    }

    /**
     * Constructor for testing purposes.
     */
    private AuthManager() {
        // Empty constructor for testing
    }

    /**
     * Set a test instance for unit testing.
     *
     * @param authManager The mock auth manager
     */
    public static void setTestInstance(AuthManager authManager) {
        testInstance = authManager;
    }

    /**
     * Reset the test instance.
     */
    public static void resetTestInstance() {
        testInstance = null;
    }
    
    /**
     * Set a token refresh listener.
     * 
     * @param listener The token refresh listener
     */
    public void setTokenRefreshListener(TokenRefreshListener listener) {
        this.tokenRefreshListener = listener;
    }

    /**
     * Save the authentication token with expiry time.
     *
     * @param token The JWT token
     * @param refreshToken The refresh token
     * @param expiryTimeMs The token expiry time in milliseconds (Unix timestamp)
     */
    public void saveToken(String token, String refreshToken, long expiryTimeMs) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_TOKEN_EXPIRY, expiryTimeMs)
                .apply();
        
        Log.d(TAG, "Token saved with expiry at: " + expiryTimeMs);
    }

    /**
     * Get the stored authentication token.
     *
     * @return The JWT token, or null if not found
     */
    public String getToken() {
        // Check if token is expired or close to expiry
        if (isTokenExpired() || isTokenNearExpiry()) {
            // Try to refresh the token
            if (!refreshTokenInBackground()) {
                // If refresh can't be initiated, return the current token
                // This could still be valid for a short time even if it's near expiry
                return sharedPreferences.getString(KEY_TOKEN, null);
            }
        }
        
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    
    /**
     * Get the stored token without checking expiry.
     * 
     * @return The raw JWT token, or null if not found
     */
    public String getRawToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * Get the stored refresh token.
     *
     * @return The refresh token, or null if not found
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }
    
    /**
     * Get the token expiry time.
     * 
     * @return The token expiry time in milliseconds (Unix timestamp)
     */
    public long getTokenExpiryTime() {
        return sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0);
    }
    
    /**
     * Check if the token is expired.
     * 
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired() {
        long expiryTime = getTokenExpiryTime();
        if (expiryTime <= 0) {
            return getToken() == null;
        }
        
        return System.currentTimeMillis() >= expiryTime;
    }
    
    /**
     * Check if the token is near expiry.
     * 
     * @return true if the token is near expiry, false otherwise
     */
    private boolean isTokenNearExpiry() {
        long expiryTime = getTokenExpiryTime();
        if (expiryTime <= 0) {
            return false;
        }
        
        long timeUntilExpiry = expiryTime - System.currentTimeMillis();
        return timeUntilExpiry <= TOKEN_REFRESH_BUFFER_MS;
    }
    
    /**
     * Refresh the token in the background.
     * 
     * @return true if the refresh was initiated, false otherwise
     */
    private boolean refreshTokenInBackground() {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            Log.w(TAG, "Cannot refresh token: No refresh token available");
            return false;
        }
        
        Log.d(TAG, "Initiating token refresh in background");
        
        // Perform the token refresh in a background thread
        executor.execute(() -> {
            refreshTokenSync();
        });
        
        return true;
    }
    
    /**
     * Force a token refresh synchronously.
     * 
     * @return true if the token was refreshed successfully, false otherwise
     */
    public boolean refreshTokenSync() {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            Log.w(TAG, "Cannot refresh token: No refresh token available");
            if (tokenRefreshListener != null) {
                tokenRefreshListener.onTokenRefreshFailed();
            }
            return false;
        }
        
        Log.d(TAG, "Refreshing token synchronously");
        
        try {
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
            AuthService authService = ApiClient.getClient().create(AuthService.class);
            
            Response<LoginResponse> response = authService.refreshToken(request).execute();
            
            if (response.isSuccessful() && response.body() != null) {
                LoginResponse loginResponse = response.body();
                saveToken(
                    loginResponse.getToken(),
                    loginResponse.getRefreshToken(),
                    loginResponse.getExpiry()
                );
                
                Log.d(TAG, "Token refreshed successfully");
                if (tokenRefreshListener != null) {
                    tokenRefreshListener.onTokenRefreshed();
                }
                return true;
            } else {
                Log.e(TAG, "Token refresh failed: " + response.code() + " " + response.message());
                if (tokenRefreshListener != null) {
                    tokenRefreshListener.onTokenRefreshFailed();
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception during token refresh", e);
            if (tokenRefreshListener != null) {
                tokenRefreshListener.onTokenRefreshFailed();
            }
            return false;
        }
    }
    
    /**
     * Force a token refresh asynchronously.
     */
    public void refreshTokenAsync() {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            Log.w(TAG, "Cannot refresh token: No refresh token available");
            if (tokenRefreshListener != null) {
                tokenRefreshListener.onTokenRefreshFailed();
            }
            return;
        }
        
        Log.d(TAG, "Refreshing token asynchronously");
        
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        AuthService authService = ApiClient.getClient().create(AuthService.class);
        
        authService.refreshToken(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    saveToken(
                        loginResponse.getToken(),
                        loginResponse.getRefreshToken(),
                        loginResponse.getExpiry()
                    );
                    
                    Log.d(TAG, "Token refreshed successfully");
                    if (tokenRefreshListener != null) {
                        tokenRefreshListener.onTokenRefreshed();
                    }
                } else {
                    Log.e(TAG, "Token refresh failed: " + response.code() + " " + response.message());
                    if (tokenRefreshListener != null) {
                        tokenRefreshListener.onTokenRefreshFailed();
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Token refresh request failed", t);
                if (tokenRefreshListener != null) {
                    tokenRefreshListener.onTokenRefreshFailed();
                }
            }
        });
    }

    /**
     * Save the user ID.
     *
     * @param userId The user ID
     */
    public void saveUserId(Long userId) {
        sharedPreferences.edit().putLong(KEY_USER_ID, userId).apply();
    }

    /**
     * Get the stored user ID.
     *
     * @return The user ID, or -1 if not found
     */
    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, -1);
    }

    /**
     * Save the username.
     *
     * @param username The username
     */
    public void saveUsername(String username) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply();
    }

    /**
     * Get the stored username.
     *
     * @return The username, or null if not found
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    /**
     * Clear all authentication data.
     */
    public void logout() {
        sharedPreferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_TOKEN_EXPIRY)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .apply();
        
        Log.d(TAG, "User logged out, auth data cleared");
    }

    /**
     * Check if the user is logged in.
     *
     * @return true if the user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        String token = getRawToken();
        return token != null && !isTokenExpired();
    }
} 