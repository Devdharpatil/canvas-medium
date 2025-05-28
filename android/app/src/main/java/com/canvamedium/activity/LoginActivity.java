package com.canvamedium.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
        } else if (!isPasswordValid(password)) {
            editTextPassword.setError(getString(R.string.error_invalid_password));
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
        LoginRequest loginRequest = new LoginRequest(email, password);

        Call<LoginResponse> call = authService.login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                showProgress(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Save tokens and user info
                    LoginResponse loginResponse = response.body();
                    authManager.saveToken(loginResponse.getToken());
                    authManager.saveRefreshToken(loginResponse.getRefreshToken());
                    authManager.saveUserId(loginResponse.getUserId());
                    authManager.saveUsername(loginResponse.getUsername());

                    // Navigate to main activity
                    navigateToMainActivity();
                    finish();
                } else {
                    // Handle error
                    String errorMessage = response.code() == 401
                            ? getString(R.string.error_invalid_credentials)
                            : getString(R.string.error_login_failed);
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
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
        return password.length() >= 6;
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