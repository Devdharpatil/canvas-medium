package com.canvamedium.activity;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.canvamedium.R;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.AuthService;
import com.canvamedium.model.LoginResponse;
import com.canvamedium.model.RegisterRequest;
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
import org.robolectric.shadows.ShadowToast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class RegisterActivityTest {

    private RegisterActivity activity;
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextFullName;
    private Button buttonRegister;
    private TextView textViewLogin;

    @Mock
    private AuthService mockAuthService;

    @Mock
    private AuthManager mockAuthManager;

    @Mock
    private Call<LoginResponse> mockCall;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Mock API client
        ApiClient.setTestClient(mockAuthService);
        AuthManager.setTestInstance(mockAuthManager);

        // Setup mock call behavior
        when(mockAuthService.register(any(RegisterRequest.class))).thenReturn(mockCall);
        
        activity = Robolectric.buildActivity(RegisterActivity.class).create().get();
        
        // Get views
        editTextUsername = activity.findViewById(R.id.edit_text_username);
        editTextEmail = activity.findViewById(R.id.edit_text_email);
        editTextPassword = activity.findViewById(R.id.edit_text_password);
        editTextConfirmPassword = activity.findViewById(R.id.edit_text_confirm_password);
        editTextFullName = activity.findViewById(R.id.edit_text_full_name);
        buttonRegister = activity.findViewById(R.id.button_register);
        textViewLogin = activity.findViewById(R.id.text_view_login);
    }

    @Test
    public void testInitialState() {
        assertNotNull(activity);
        assertNotNull(editTextUsername);
        assertNotNull(editTextEmail);
        assertNotNull(editTextPassword);
        assertNotNull(editTextConfirmPassword);
        assertNotNull(editTextFullName);
        assertNotNull(buttonRegister);
        assertNotNull(textViewLogin);
    }

    @Test
    public void testEmptyFields_ShowsErrors() {
        buttonRegister.performClick();
        
        assertTrue(editTextUsername.getError() != null);
        assertTrue(editTextEmail.getError() != null);
        assertTrue(editTextPassword.getError() != null);
        assertTrue(editTextConfirmPassword.getError() != null);
        assertTrue(editTextFullName.getError() != null);
    }
    
    @Test
    public void testInvalidEmail_ShowsError() {
        editTextUsername.setText("validuser");
        editTextEmail.setText("invalid-email");
        editTextPassword.setText("password123");
        editTextConfirmPassword.setText("password123");
        editTextFullName.setText("Full Name");
        
        buttonRegister.performClick();
        
        assertNotNull(editTextEmail.getError());
        assertEquals(activity.getString(R.string.error_invalid_email), editTextEmail.getError().toString());
    }

    @Test
    public void testPasswordMismatch_ShowsError() {
        editTextUsername.setText("validuser");
        editTextEmail.setText("valid@email.com");
        editTextPassword.setText("password123");
        editTextConfirmPassword.setText("differentpassword");
        editTextFullName.setText("Full Name");
        
        buttonRegister.performClick();
        
        assertNotNull(editTextConfirmPassword.getError());
        assertEquals(activity.getString(R.string.error_passwords_do_not_match), 
                editTextConfirmPassword.getError().toString());
    }
    
    @Test
    public void testShortUsername_ShowsError() {
        editTextUsername.setText("us");  // Too short
        editTextEmail.setText("valid@email.com");
        editTextPassword.setText("password123");
        editTextConfirmPassword.setText("password123");
        editTextFullName.setText("Full Name");
        
        buttonRegister.performClick();
        
        assertNotNull(editTextUsername.getError());
        assertEquals(activity.getString(R.string.error_invalid_username), 
                editTextUsername.getError().toString());
    }

    @Test
    public void testLoginLinkClick_NavigatesToLoginActivity() {
        textViewLogin.performClick();
        
        assertTrue(activity.isFinishing());
    }

    @Test
    public void testValidRegistration_CallsApi() {
        // Setup valid form data
        editTextUsername.setText("validuser");
        editTextEmail.setText("valid@email.com");
        editTextPassword.setText("password123");
        editTextConfirmPassword.setText("password123");
        editTextFullName.setText("Full Name");
        
        // Mock call behavior
        when(mockCall.enqueue(any())).thenAnswer(invocation -> {
            Callback<LoginResponse> callback = invocation.getArgument(0);
            LoginResponse response = new LoginResponse();
            response.setToken("test_token");
            response.setRefreshToken("test_refresh_token");
            response.setUserId(1L);
            response.setUsername("validuser");
            
            callback.onResponse(mockCall, Response.success(response));
            return null;
        });
        
        buttonRegister.performClick();
        
        // Verify API call was made with correct data
        verify(mockAuthService).register(any(RegisterRequest.class));
        
        // Verify token storage
        verify(mockAuthManager).saveToken("test_token");
        verify(mockAuthManager).saveRefreshToken("test_refresh_token");
        verify(mockAuthManager).saveUserId(1L);
        verify(mockAuthManager).saveUsername("validuser");
        
        // Verify toast message
        assertEquals(activity.getString(R.string.registration_successful), ShadowToast.getTextOfLatestToast());
        
        // Verify navigation
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertNotNull(startedIntent);
    }

    @Test
    public void testRegistrationError_ShowsToast() {
        // Setup valid form data
        editTextUsername.setText("validuser");
        editTextEmail.setText("valid@email.com");
        editTextPassword.setText("password123");
        editTextConfirmPassword.setText("password123");
        editTextFullName.setText("Full Name");
        
        // Mock call behavior for error (409 Conflict)
        when(mockCall.enqueue(any())).thenAnswer(invocation -> {
            Callback<LoginResponse> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.error(409, mock(okhttp3.ResponseBody.class)));
            return null;
        });
        
        buttonRegister.performClick();
        
        // Verify error toast
        assertEquals(activity.getString(R.string.error_username_email_taken), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testNetworkError_ShowsToast() {
        // Setup valid form data
        editTextUsername.setText("validuser");
        editTextEmail.setText("valid@email.com");
        editTextPassword.setText("password123");
        editTextConfirmPassword.setText("password123");
        editTextFullName.setText("Full Name");
        
        // Mock call behavior for network error
        when(mockCall.enqueue(any())).thenAnswer(invocation -> {
            Callback<LoginResponse> callback = invocation.getArgument(0);
            callback.onFailure(mockCall, new Exception("Network error"));
            return null;
        });
        
        buttonRegister.performClick();
        
        // Verify error toast
        assertEquals(activity.getString(R.string.error_network), ShadowToast.getTextOfLatestToast());
    }
} 