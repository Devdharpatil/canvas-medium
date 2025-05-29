package com.canvamedium.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.canvamedium.R;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.AuthService;
import com.canvamedium.model.LoginRequest;
import com.canvamedium.model.LoginResponse;
import com.canvamedium.util.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for user login with form validation.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView textViewRegister;
    private TextView textViewForgotPassword;

    private AuthService authService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        buttonLogin = findViewById(R.id.button_login);
        progressBar = findViewById(R.id.progress_bar);
        textViewRegister = findViewById(R.id.text_view_register);
        textViewForgotPassword = findViewById(R.id.text_view_forgot_password);

        // Initialize services
        authService = ApiClient.getClient().create(AuthService.class);
        authManager = AuthManager.getInstance(this);

        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            navigateToMainActivity();
            finish();
            return;
        }

        // Set click listeners
        buttonLogin.setOnClickListener(view -> attemptLogin());
        textViewRegister.setOnClickListener(view -> navigateToRegister());
        textViewForgotPassword.setOnClickListener(view -> navigateToForgotPassword());
    }

    /**
     * Validates input fields and attempts to log in the user.
     */
    private void attemptLogin() {
        // Reset errors
        editTextEmail.setError(null);
        editTextPassword.setError(null);

        // Get values
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input
        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.error_field_required));
            focusView = editTextPassword;
            cancel = true;
        }

        // Check for a valid email address
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError(getString(R.string.error_field_required));
            focusView = editTextEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            editTextEmail.setError(getString(R.string.error_invalid_email));
            focusView = editTextEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner and perform the login attempt
            showProgress(true);
            performLogin(email, password);
        }
    }

    /**
     * Performs the login request to the API.
     *
     * @param email    The user's email
     * @param password The user's password
     */
    private void performLogin(String email, String password) {
        // Create login request with email (backend will use this for authentication)
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Add debug log to see what's being sent
        Log.d("LoginActivity", "Sending login request: Email: " + email + ", Password: " + password);

        // For development only: try to reset the password first to ensure it matches
        // This should be removed in production
        tryResetPasswordForDev(email, password);
    }
    
    /**
     * Development-only method to reset password before login
     * This should be removed in production
     */
    private void tryResetPasswordForDev(String email, String password) {
        // Log a message so we know this was attempted
        Log.d("LoginActivity", "Development: Attempting to reset password for " + email);
        
        // Just proceed with login - the actual reset would require more code
        // This is a placeholder for where we would call the dev endpoint
        proceedWithLogin(email, password);
    }
    
    /**
     * Proceed with the actual login attempt
     */
    private void proceedWithLogin(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        Call<LoginResponse> call = authService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Save tokens and user info
                    LoginResponse loginResponse = response.body();
                    authManager.saveToken(
                        loginResponse.getToken(),
                        loginResponse.getRefreshToken(),
                        loginResponse.getExpiry()
                    );
                    authManager.saveUserId(loginResponse.getUserId());
                    authManager.saveUsername(loginResponse.getUsername());

                    // Navigate to main activity
                    navigateToMainActivity();
                    finish();
                } else {
                    // Handle error response
                    handleLoginError(response);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Network error during login: " + t.getMessage());
            }
        });
    }
    
    /**
     * Handle error response from login attempt
     */
    private void handleLoginError(Response<LoginResponse> response) {
        int responseCode = response.code();
        String errorMessage;
        
        // Try to parse the error message from the response body
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                // Simple extraction of message value from JSON
                if (errorBody.contains("\"message\":")) {
                    int start = errorBody.indexOf("\"message\":\"") + 11;
                    int end = errorBody.indexOf("\"", start);
                    if (start > 0 && end > start) {
                        errorMessage = errorBody.substring(start, end);
                    } else {
                        errorMessage = getDefaultErrorMessage(responseCode);
                    }
                } else {
                    errorMessage = getDefaultErrorMessage(responseCode);
                }
            } else {
                errorMessage = getDefaultErrorMessage(responseCode);
            }
        } catch (Exception e) {
            errorMessage = getDefaultErrorMessage(responseCode);
        }
        
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        Log.e("LoginActivity", "Login failed with code " + responseCode + ": " + errorMessage);
    }

    /**
     * Returns a default error message based on response code.
     *
     * @param responseCode The HTTP response code
     * @return An appropriate error message
     */
    private String getDefaultErrorMessage(int responseCode) {
        switch (responseCode) {
            case 401:
                return getString(R.string.error_invalid_credentials);
            case 400:
                return getString(R.string.error_invalid_login_data);
            case 500:
                return getString(R.string.error_server);
            default:
                return getString(R.string.error_login_failed);
        }
    }

    /**
     * Shows or hides the progress UI.
     *
     * @param show true to show progress, false to hide
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonLogin.setEnabled(!show);
    }

    /**
     * Validates the email format.
     *
     * @param email The email to validate
     * @return true if the email is valid
     */
    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates the password.
     *
     * @param password The password to validate
     * @return true if the password is valid
     */
    private boolean isPasswordValid(String password) {
        // Allow any non-empty password
        return !password.isEmpty();
    }

    /**
     * Navigates to the registration screen.
     */
    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Navigates to the forgot password screen.
     */
    private void navigateToForgotPassword() {
        // Not implemented yet
        Toast.makeText(this, getString(R.string.feature_not_available), Toast.LENGTH_SHORT).show();
    }

    /**
     * Navigates to the main activity.
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 