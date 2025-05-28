package com.canvamedium.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.canvamedium.R;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.UserService;
import com.canvamedium.model.UserProfile;
import com.canvamedium.util.AuthManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for displaying and managing user profile.
 */
public class UserProfileActivity extends AppCompatActivity {

    private TextView textUsername;
    private TextView textFullName;
    private TextView textBio;
    private TextView textArticleCount;
    private TextView textDraftCount;
    private TextView textTemplateCount;
    private TextView textEmail;
    private TextView textEmailVerified;
    private TextView textJoinDate;
    private TextView textLastLogin;
    private CircleImageView imageProfile;
    private Button buttonEditProfile;
    private MaterialButton buttonSettings;
    private Button buttonLogout;
    private FloatingActionButton fabEditProfileImage;
    private ProgressBar progressBar;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private UserService userService;
    private AuthManager authManager;
    private UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize views
        textUsername = findViewById(R.id.text_username);
        textFullName = findViewById(R.id.text_full_name);
        textBio = findViewById(R.id.text_bio);
        textArticleCount = findViewById(R.id.text_article_count);
        textDraftCount = findViewById(R.id.text_draft_count);
        textTemplateCount = findViewById(R.id.text_template_count);
        textEmail = findViewById(R.id.text_email);
        textEmailVerified = findViewById(R.id.text_email_verified);
        textJoinDate = findViewById(R.id.text_join_date);
        textLastLogin = findViewById(R.id.text_last_login);
        imageProfile = findViewById(R.id.image_profile);
        buttonEditProfile = findViewById(R.id.button_edit_profile);
        buttonSettings = findViewById(R.id.button_settings);
        buttonLogout = findViewById(R.id.button_logout);
        fabEditProfileImage = findViewById(R.id.fab_edit_profile_image);
        progressBar = findViewById(R.id.progress_bar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

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
        buttonEditProfile.setOnClickListener(v -> navigateToEditProfile());
        buttonSettings.setOnClickListener(v -> navigateToSettings());
        buttonLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
        fabEditProfileImage.setOnClickListener(v -> showImagePickerDialog());

        // Load user profile
        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile when returning from edit activity
        if (userProfile != null) {
            loadUserProfile();
        }
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
     * Loads the user profile from the API.
     */
    private void loadUserProfile() {
        showProgress(true);

        Call<UserProfile> call = userService.getCurrentUserProfile();
        call.enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    userProfile = response.body();
                    displayUserProfile(userProfile);
                } else {
                    Toast.makeText(UserProfileActivity.this, getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                showProgress(false);
                Toast.makeText(UserProfileActivity.this, getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays the user profile data in the UI.
     *
     * @param profile The user profile to display
     */
    private void displayUserProfile(UserProfile profile) {
        collapsingToolbarLayout.setTitle(profile.getUsername());
        textUsername.setText("@" + profile.getUsername());
        textFullName.setText(profile.getFullName());
        
        // Bio might be null
        String bio = profile.getBio();
        textBio.setText(bio != null && !bio.isEmpty() ? bio : getString(R.string.bio_placeholder));
        
        textArticleCount.setText(String.valueOf(profile.getArticleCount()));
        textDraftCount.setText(String.valueOf(profile.getDraftCount()));
        textTemplateCount.setText(String.valueOf(profile.getTemplateCount()));
        
        textEmail.setText(profile.getEmail());
        textEmailVerified.setText(profile.isEmailVerified() ? getString(R.string.yes) : getString(R.string.no));
        
        textJoinDate.setText(profile.getJoinDate() != null ? profile.getJoinDate() : getString(R.string.date_placeholder));
        textLastLogin.setText(profile.getLastLoginDate() != null ? profile.getLastLoginDate() : getString(R.string.date_placeholder));
        
        // Load profile image if available
        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) {
            // Using Glide to load image (add implementation in your project)
            com.bumptech.glide.Glide.with(this)
                    .load(profile.getProfileImageUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(imageProfile);
        }
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
     * Navigates to the edit profile screen.
     */
    private void navigateToEditProfile() {
        // In a real app, start an edit profile activity
        Toast.makeText(this, getString(R.string.feature_not_available), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Navigates to the settings screen.
     */
    private void navigateToSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Shows a confirmation dialog before logging out.
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Performs the logout operation.
     */
    private void performLogout() {
        authManager.clearAuthData();
        navigateToLogin();
        finish();
    }

    /**
     * Shows a dialog to pick a profile image.
     */
    private void showImagePickerDialog() {
        // In a real app, show image picker options
        Toast.makeText(this, getString(R.string.feature_not_available), Toast.LENGTH_SHORT).show();
    }

    /**
     * Navigates to the login screen.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 