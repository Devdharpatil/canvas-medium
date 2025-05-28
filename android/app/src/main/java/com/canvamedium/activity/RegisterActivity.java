package com.canvamedium.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
import com.canvamedium.model.LoginResponse;
import com.canvamedium.model.RegisterRequest;
import com.canvamedium.util.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for user registration with form validation.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextFullName;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private TextView textViewLogin;

    private AuthService authService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        editTextUsername = findViewById(R.id.edit_text_username);
        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        editTextFullName = findViewById(R.id.edit_text_full_name);
        buttonRegister = findViewById(R.id.button_register);
        progressBar = findViewById(R.id.progress_bar);
        textViewLogin = findViewById(R.id.text_view_login);

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
        buttonRegister.setOnClickListener(view -> attemptRegister());
        textViewLogin.setOnClickListener(view -> navigateToLogin());
    }

    /**
     * Validates input fields and attempts to register the user.
     */
    private void attemptRegister() {
        // Reset errors
        editTextUsername.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);
        editTextFullName.setError(null);

        // Get values
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();

        // Validate input
        boolean cancel = false;
        View focusView = null;

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError(getString(R.string.error_field_required));
            focusView = editTextUsername;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            editTextUsername.setError(getString(R.string.error_invalid_username));
            focusView = editTextUsername;
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

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError(getString(R.string.error_field_required));
            focusView = editTextPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            editTextPassword.setError(getString(R.string.error_invalid_password));
            focusView = editTextPassword;
            cancel = true;
        }

        // Check if passwords match
        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = editTextConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError(getString(R.string.error_passwords_do_not_match));
            focusView = editTextConfirmPassword;
            cancel = true;
        }

        // Check for a valid full name
        if (TextUtils.isEmpty(fullName)) {
            editTextFullName.setError(getString(R.string.error_field_required));
            focusView = editTextFullName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner and perform the registration attempt
            showProgress(true);
            performRegister(username, email, password, fullName);
        }
    }

    /**
     * Performs the registration request to the API.
     *
     * @param username The user's username
     * @param email    The user's email
     * @param password The user's password
     * @param fullName The user's full name
     */
    private void performRegister(String username, String email, String password, String fullName) {
        RegisterRequest registerRequest = new RegisterRequest(username, email, password, fullName);

        Call<LoginResponse> call = authService.register(registerRequest);
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

                    // Show success message and navigate to main activity
                    Toast.makeText(RegisterActivity.this, getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                    finish();
                } else {
                    // Handle error based on HTTP status code
                    String errorMessage;
                    
                    switch (response.code()) {
                        case 409:
                            // Conflict - username or email already exists
                            try {
                                String responseBody = response.errorBody().string();
                                if (responseBody.contains("Username")) {
                                    errorMessage = getString(R.string.error_username_taken);
                                    editTextUsername.setError(errorMessage);
                                    editTextUsername.requestFocus();
                                } else if (responseBody.contains("Email")) {
                                    errorMessage = getString(R.string.error_email_taken);
                                    editTextEmail.setError(errorMessage);
                                    editTextEmail.requestFocus();
                                } else {
                                    errorMessage = getString(R.string.error_username_email_taken);
                                }
                            } catch (Exception e) {
                                errorMessage = getString(R.string.error_username_email_taken);
                            }
                            break;
                            
                        case 400:
                            // Bad request - validation errors
                            errorMessage = getString(R.string.error_invalid_registration_data);
                            break;
                            
                        case 500:
                        case 502:
                        case 503:
                        case 504:
                            // Server errors
                            errorMessage = getString(R.string.error_server);
                            break;
                            
                        default:
                            errorMessage = getString(R.string.error_registration_failed);
                            break;
                    }
                    
                    showErrorMessage(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showProgress(false);
                
                // Determine the type of failure
                String errorMessage;
                if (!isNetworkAvailable()) {
                    errorMessage = getString(R.string.error_no_internet);
                } else if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                    errorMessage = getString(R.string.error_timeout);
                } else {
                    errorMessage = getString(R.string.error_network);
                }
                
                showErrorMessage(errorMessage);
            }
        });
    }

    /**
     * Displays an error message to the user.
     *
     * @param message The error message to display
     */
    private void showErrorMessage(String message) {
        // Display a toast message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        // You could also update a TextView to show the error more prominently
        // textViewError.setText(message);
        // textViewError.setVisibility(View.VISIBLE);
    }
    
    /**
     * Checks if the device has an active network connection.
     *
     * @return true if a network connection is available
     */
    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) 
                getSystemService(CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Shows or hides the progress UI.
     *
     * @param show true to show progress, false to hide
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        buttonRegister.setEnabled(!show);
    }

    /**
     * Validates the email format.
     *
     * @param email The email to validate
     * @return true if the email is valid
     */
    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates the password.
     *
     * @param password The password to validate
     * @return true if the password is valid
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    /**
     * Validates the username.
     *
     * @param username The username to validate
     * @return true if the username is valid
     */
    private boolean isUsernameValid(String username) {
        // Username should be at least 3 characters and only contain alphanumeric and underscore
        return username.length() >= 3 && username.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * Navigates to the login screen.
     */
    private void navigateToLogin() {
        finish(); // Since login activity is likely the previous activity
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