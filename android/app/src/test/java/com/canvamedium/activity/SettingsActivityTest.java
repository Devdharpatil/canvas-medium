package com.canvamedium.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.canvamedium.R;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.UserService;
import com.canvamedium.model.SettingsUpdateRequest;
import com.canvamedium.model.UserProfile;
import com.canvamedium.util.AuthManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SettingsActivityTest {

    private SettingsActivity activity;

    @Mock
    private UserService userService;

    @Mock
    private AuthManager authManager;

    @Mock
    private Call<UserProfile> userProfileCall;

    @Mock
    private Call<Void> voidCall;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Mock API client
        ApiClient.setMockMode(true);
        ApiClient.setMockUserService(userService);

        // Mock auth manager
        AuthManager.setMockInstance(authManager);
        when(authManager.isLoggedIn()).thenReturn(true);

        // Mock user profile call
        when(userService.getCurrentUserProfile()).thenReturn(userProfileCall);
        
        // Create the activity
        activity = Robolectric.buildActivity(SettingsActivity.class).create().start().resume().get();
    }

    @Test
    public void testActivityCreated() {
        assertNotNull(activity);
    }

    @Test
    public void testInitialUiState() {
        Switch switchNotifications = activity.findViewById(R.id.switch_notifications);
        Switch switchDarkMode = activity.findViewById(R.id.switch_dark_mode);
        Switch switchEmailUpdates = activity.findViewById(R.id.switch_email_updates);
        Button buttonSaveSettings = activity.findViewById(R.id.button_save_settings);
        Button buttonChangePassword = activity.findViewById(R.id.button_change_password);

        assertNotNull(switchNotifications);
        assertNotNull(switchDarkMode);
        assertNotNull(switchEmailUpdates);
        assertNotNull(buttonSaveSettings);
        assertNotNull(buttonChangePassword);
    }

    @Test
    public void testLoadUserSettings_Success() {
        // Create a mock user profile
        UserProfile mockProfile = new UserProfile();
        mockProfile.setNotificationsEnabled(true);
        mockProfile.setEmailUpdatesEnabled(false);

        // Set up the mock call to respond with success
        doAnswer(invocation -> {
            Callback<UserProfile> callback = invocation.getArgument(0);
            callback.onResponse(userProfileCall, Response.success(mockProfile));
            return null;
        }).when(userProfileCall).enqueue(any());

        // Trigger loading settings
        activity.loadUserSettings();

        // Verify UI is updated
        Switch switchNotifications = activity.findViewById(R.id.switch_notifications);
        Switch switchEmailUpdates = activity.findViewById(R.id.switch_email_updates);

        assertTrue(switchNotifications.isChecked());
        assertFalse(switchEmailUpdates.isChecked());
    }

    @Test
    public void testSaveSettings_Success() {
        // Mock the service call
        when(userService.updateSettings(any(SettingsUpdateRequest.class))).thenReturn(userProfileCall);
        
        // Set up the mock call to respond with success
        doAnswer(invocation -> {
            Callback<UserProfile> callback = invocation.getArgument(0);
            callback.onResponse(userProfileCall, Response.success(new UserProfile()));
            return null;
        }).when(userProfileCall).enqueue(any());

        // Toggle switches
        Switch switchNotifications = activity.findViewById(R.id.switch_notifications);
        Switch switchEmailUpdates = activity.findViewById(R.id.switch_email_updates);
        
        switchNotifications.setChecked(true);
        switchEmailUpdates.setChecked(true);
        
        // Save settings
        Button buttonSaveSettings = activity.findViewById(R.id.button_save_settings);
        buttonSaveSettings.performClick();
        
        // Verify service call was made
        verify(userService).updateSettings(any(SettingsUpdateRequest.class));
        
        // Verify success toast is shown
        assertEquals("Settings saved successfully", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testChangePassword_ValidationSuccess() {
        // Mock the service call
        when(userService.changePassword(any(SettingsUpdateRequest.class))).thenReturn(voidCall);
        
        // Set up the mock call to respond with success
        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(0);
            callback.onResponse(voidCall, Response.success(null));
            return null;
        }).when(voidCall).enqueue(any());

        // Set password fields
        EditText currentPassword = activity.findViewById(R.id.edit_text_current_password);
        EditText newPassword = activity.findViewById(R.id.edit_text_new_password);
        EditText confirmPassword = activity.findViewById(R.id.edit_text_confirm_password);
        
        currentPassword.setText("oldpass123");
        newPassword.setText("newpass123");
        confirmPassword.setText("newpass123");
        
        // Change password
        Button buttonChangePassword = activity.findViewById(R.id.button_change_password);
        buttonChangePassword.performClick();
        
        // Verify service call was made
        verify(userService).changePassword(any(SettingsUpdateRequest.class));
        
        // Verify success toast is shown
        assertEquals("Password changed successfully", ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testChangePassword_ValidationFailure() {
        // Set password fields with non-matching passwords
        EditText currentPassword = activity.findViewById(R.id.edit_text_current_password);
        EditText newPassword = activity.findViewById(R.id.edit_text_new_password);
        EditText confirmPassword = activity.findViewById(R.id.edit_text_confirm_password);
        
        currentPassword.setText("oldpass123");
        newPassword.setText("newpass123");
        confirmPassword.setText("different123");
        
        // Change password
        Button buttonChangePassword = activity.findViewById(R.id.button_change_password);
        buttonChangePassword.performClick();
        
        // Verify error is shown
        assertEquals("Passwords do not match", confirmPassword.getError().toString());
    }
} 