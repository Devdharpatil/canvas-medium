package com.canvamedium.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.canvamedium.R;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.UserService;
import com.canvamedium.model.SettingsUpdateRequest;
import com.canvamedium.model.UserProfile;
import com.canvamedium.util.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for managing user account settings.
 */
public class SettingsActivity extends AppCompatActivity {

    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Switch switchNotifications;
    private Switch switchDarkMode;
    private Switch switchEmailUpdates;
    private Button buttonSaveSettings;
    private Button buttonChangePassword;
    private ProgressBar progressBar;

    private UserService userService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Account Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Initialize views
        editTextCurrentPassword = findViewById(R.id.edit_text_current_password);
        editTextNewPassword = findViewById(R.id.edit_text_new_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchEmailUpdates = findViewById(R.id.switch_email_updates);
        buttonSaveSettings = findViewById(R.id.button_save_settings);
        buttonChangePassword = findViewById(R.id.button_change_password);
        progressBar = findViewById(R.id.progress_bar);

        // Initialize services
        userService = ApiClient.getAuthenticatedClient(this).create(UserService.class);
        authManager = AuthManager.getInstance(this);

        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            finish();
            return;
        }

        // Set click listeners
        buttonSaveSettings.setOnClickListener(v -> saveSettings());
        buttonChangePassword.setOnClickListener(v -> changePassword());

        // Load current settings
        loadUserSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Loads the user settings from the API.
     */
    public void loadUserSettings() {
        showProgress(true);

        Call<UserProfile> call = userService.getCurrentUserProfile();
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    displayUserSettings(response.body());
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to load settings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                showProgress(false);
                Toast.makeText(SettingsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays user settings in the UI.
     *
     * @param profile The user profile containing settings
     */
    private void displayUserSettings(UserProfile profile) {
        // In a real app, populate the settings from the profile
        // Here we're assuming the profile has these boolean properties
        switchNotifications.setChecked(profile.isNotificationsEnabled());
        switchEmailUpdates.setChecked(profile.isEmailUpdatesEnabled());
        
        // Dark mode setting might be stored in SharedPreferences instead
        boolean darkModeEnabled = getSharedPreferences("app_preferences", MODE_PRIVATE)
                .getBoolean("dark_mode_enabled", false);
        switchDarkMode.setChecked(darkModeEnabled);
    }

    /**
     * Saves the user settings.
     */
    private void saveSettings() {
        showProgress(true);

        SettingsUpdateRequest request = new SettingsUpdateRequest();
        request.setNotificationsEnabled(switchNotifications.isChecked());
        request.setEmailUpdatesEnabled(switchEmailUpdates.isChecked());

        Call<UserProfile> call = userService.updateSettings(request);
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    // Save dark mode setting to SharedPreferences
                    getSharedPreferences("app_preferences", MODE_PRIVATE)
                            .edit()
                            .putBoolean("dark_mode_enabled", switchDarkMode.isChecked())
                            .apply();
                    
                    Toast.makeText(SettingsActivity.this, "Settings saved successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Return to previous screen
                } else {
                    Toast.makeText(SettingsActivity.this, "Failed to save settings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                showProgress(false);
                Toast.makeText(SettingsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Changes the user password.
     */
    private void changePassword() {
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        
        // Basic validation
        if (currentPassword.isEmpty()) {
            editTextCurrentPassword.setError("Current password is required");
            return;
        }
        
        if (newPassword.isEmpty()) {
            editTextNewPassword.setError("New password is required");
            return;
        }
        
        if (newPassword.length() < 6) {
            editTextNewPassword.setError("Password must be at least 6 characters");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            return;
        }
        
        showProgress(true);
        
        // Create request for password change
        SettingsUpdateRequest request = new SettingsUpdateRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        
        Call<Void> call = userService.changePassword(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showProgress(false);
                if (response.isSuccessful()) {
                    Toast.makeText(SettingsActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    clearPasswordFields();
                } else {
                    int statusCode = response.code();
                    if (statusCode == 401) {
                        editTextCurrentPassword.setError("Current password is incorrect");
                    } else {
                        Toast.makeText(SettingsActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showProgress(false);
                Toast.makeText(SettingsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Clears the password fields.
     */
    private void clearPasswordFields() {
        editTextCurrentPassword.setText("");
        editTextNewPassword.setText("");
        editTextConfirmPassword.setText("");
    }

    /**
     * Shows the progress indicator.
     *
     * @param show true to show progress, false to hide
     */
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Navigates to the login screen.
     */
    private void navigateToLogin() {
        // In a real app, navigate to login activity
    }
} 