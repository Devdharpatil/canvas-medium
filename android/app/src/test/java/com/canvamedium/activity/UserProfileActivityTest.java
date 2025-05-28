package com.canvamedium.activity;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;

import com.canvamedium.R;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.UserService;
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
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class UserProfileActivityTest {

    private UserProfileActivity activity;
    
    @Mock
    private UserService mockUserService;
    
    @Mock
    private AuthManager mockAuthManager;
    
    @Mock
    private Call<UserProfile> mockCall;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Mock API client
        ApiClient.setTestClient(mockUserService);
        AuthManager.setTestInstance(mockAuthManager);
        
        // Setup mock call behavior
        when(mockUserService.getCurrentUserProfile()).thenReturn(mockCall);
        
        // Mock auth manager to return logged in state
        when(mockAuthManager.isLoggedIn()).thenReturn(true);
        
        // Create the activity
        activity = Robolectric.buildActivity(UserProfileActivity.class).create().get();
    }
    
    @Test
    public void testInitialState() {
        assertNotNull(activity);
        
        // Verify UI components are initialized
        assertNotNull(activity.findViewById(R.id.text_username));
        assertNotNull(activity.findViewById(R.id.text_full_name));
        assertNotNull(activity.findViewById(R.id.text_bio));
        assertNotNull(activity.findViewById(R.id.button_edit_profile));
        assertNotNull(activity.findViewById(R.id.button_logout));
        
        // Verify API call was made
        verify(mockUserService).getCurrentUserProfile();
    }
    
    @Test
    public void testDisplayUserProfile() {
        // Create a mock response
        UserProfile mockProfile = new UserProfile();
        mockProfile.setUsername("testuser");
        mockProfile.setFullName("Test User");
        mockProfile.setBio("This is a test bio");
        mockProfile.setEmail("test@example.com");
        mockProfile.setArticleCount(5);
        mockProfile.setDraftCount(3);
        mockProfile.setTemplateCount(2);
        mockProfile.setEmailVerified(true);
        mockProfile.setJoinDate("Jan 1, 2023");
        mockProfile.setLastLoginDate("May 15, 2023");
        
        // Simulate successful API response
        when(mockCall.enqueue(any())).thenAnswer(invocation -> {
            Callback<UserProfile> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.success(mockProfile));
            return null;
        });
        
        // Reload profile
        activity.onResume();
        
        // Verify UI is updated with profile data
        TextView textUsername = activity.findViewById(R.id.text_username);
        TextView textFullName = activity.findViewById(R.id.text_full_name);
        TextView textBio = activity.findViewById(R.id.text_bio);
        TextView textEmail = activity.findViewById(R.id.text_email);
        TextView textArticleCount = activity.findViewById(R.id.text_article_count);
        TextView textEmailVerified = activity.findViewById(R.id.text_email_verified);
        
        assertEquals("@testuser", textUsername.getText().toString());
        assertEquals("Test User", textFullName.getText().toString());
        assertEquals("This is a test bio", textBio.getText().toString());
        assertEquals("test@example.com", textEmail.getText().toString());
        assertEquals("5", textArticleCount.getText().toString());
        assertEquals(activity.getString(R.string.yes), textEmailVerified.getText().toString());
    }
    
    @Test
    public void testLogoutConfirmationDialog() {
        // Click the logout button
        Button buttonLogout = activity.findViewById(R.id.button_logout);
        buttonLogout.performClick();
        
        // Verify dialog is shown
        ShadowAlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        
        // Verify dialog title and message
        ShadowAlertDialog shadowDialog = shadowOf(dialog);
        assertEquals("Logout", shadowDialog.getTitle());
        assertTrue(shadowDialog.getMessage().toString().contains("Are you sure"));
    }
    
    @Test
    public void testLogout() {
        // Setup logout behavior
        when(mockAuthManager.clearAuthData()).thenReturn(null);
        
        // Click logout button and confirm
        Button buttonLogout = activity.findViewById(R.id.button_logout);
        buttonLogout.performClick();
        
        ShadowAlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick();
        
        // Verify auth manager was called to clear data
        verify(mockAuthManager).clearAuthData();
        
        // Verify navigation to login activity
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(LoginActivity.class.getName(), startedIntent.getComponent().getClassName());
    }
    
    @Test
    public void testEditProfileNotAvailable() {
        // Click edit profile button
        Button buttonEditProfile = activity.findViewById(R.id.button_edit_profile);
        buttonEditProfile.performClick();
        
        // Verify toast message
        assertEquals(activity.getString(R.string.feature_not_available), ShadowToast.getTextOfLatestToast());
    }
    
    @Test
    public void testApiError() {
        // Simulate API error
        when(mockCall.enqueue(any())).thenAnswer(invocation -> {
            Callback<UserProfile> callback = invocation.getArgument(0);
            callback.onFailure(mockCall, new Exception("Network error"));
            return null;
        });
        
        // Reload profile
        activity.onResume();
        
        // Verify error toast
        assertEquals(activity.getString(R.string.error_network), ShadowToast.getTextOfLatestToast());
    }
} 