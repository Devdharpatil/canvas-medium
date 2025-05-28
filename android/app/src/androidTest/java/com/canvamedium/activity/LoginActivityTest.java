package com.canvamedium.activity;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.canvamedium.R;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.AuthService;
import com.canvamedium.model.LoginRequest;
import com.canvamedium.model.LoginResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * UI tests for the LoginActivity
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityTest {

    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "Password123";
    private static final String INVALID_EMAIL = "not_an_email";
    private static final String SHORT_PASSWORD = "123";
    private static final String ERROR_MESSAGE = "Invalid credentials";

    private CountingIdlingResource idlingResource = new CountingIdlingResource("LoginActivity");

    @Mock
    private AuthService mockAuthService;

    @Mock
    private Call<LoginResponse> mockCall;

    @Rule
    public ActivityScenarioRule<LoginActivity> activityScenarioRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Register idling resource
        IdlingRegistry.getInstance().register(idlingResource);
        
        // Setup mock responses
        ApiClient.setMockMode(true);
        ApiClient.setMockAuthService(mockAuthService);
    }

    @After
    public void tearDown() {
        // Unregister idling resource
        IdlingRegistry.getInstance().unregister(idlingResource);
        
        // Reset mock mode
        ApiClient.resetMockServices();
    }

    @Test
    public void testLoginWithEmptyCredentials() {
        // Click on login button without entering credentials
        onView(withId(R.id.buttonLogin)).perform(click());
        
        // Check that error messages are displayed
        onView(withId(R.id.textInputLayoutEmail))
                .check(matches(hasTextInputError("Email is required")));
        
        onView(withId(R.id.textInputLayoutPassword))
                .check(matches(hasTextInputError("Password is required")));
    }

    @Test
    public void testLoginWithInvalidEmail() {
        // Type invalid email and valid password
        onView(withId(R.id.editTextEmail)).perform(typeText(INVALID_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText(VALID_PASSWORD), closeSoftKeyboard());
        
        // Click login button
        onView(withId(R.id.buttonLogin)).perform(click());
        
        // Check that email error is displayed
        onView(withId(R.id.textInputLayoutEmail))
                .check(matches(hasTextInputError("Invalid email format")));
    }

    @Test
    public void testLoginWithShortPassword() {
        // Type valid email and short password
        onView(withId(R.id.editTextEmail)).perform(typeText(VALID_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText(SHORT_PASSWORD), closeSoftKeyboard());
        
        // Click login button
        onView(withId(R.id.buttonLogin)).perform(click());
        
        // Check that password error is displayed
        onView(withId(R.id.textInputLayoutPassword))
                .check(matches(hasTextInputError("Password must be at least 6 characters")));
    }

    @Test
    public void testLoginWithValidCredentials_Success() throws IOException {
        // Setup mock response for successful login
        LoginResponse successResponse = new LoginResponse();
        successResponse.setToken("test_token");
        successResponse.setRefreshToken("test_refresh_token");
        successResponse.setExpiry(System.currentTimeMillis() + 3600000); // 1 hour from now
        
        when(mockAuthService.login(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(successResponse));
        
        // Type valid credentials
        onView(withId(R.id.editTextEmail)).perform(typeText(VALID_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText(VALID_PASSWORD), closeSoftKeyboard());
        
        // Click login button
        onView(withId(R.id.buttonLogin)).perform(click());
        
        // Verify navigation to MainActivity
        // Note: Need to add Espresso-Intents to the project for this
        // intended(hasComponent(MainActivity.class.getName()));
    }

    @Test
    public void testLoginWithValidCredentials_Error() throws IOException {
        // Setup mock response for failed login
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), 
                "{\"message\":\"" + ERROR_MESSAGE + "\"}");
        
        when(mockAuthService.login(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.error(401, errorBody));
        
        // Type valid credentials
        onView(withId(R.id.editTextEmail)).perform(typeText(VALID_EMAIL), closeSoftKeyboard());
        onView(withId(R.id.editTextPassword)).perform(typeText(VALID_PASSWORD), closeSoftKeyboard());
        
        // Click login button
        onView(withId(R.id.buttonLogin)).perform(click());
        
        // Check that error message is displayed
        onView(withId(R.id.textViewError)).check(matches(withText(ERROR_MESSAGE)));
    }

    @Test
    public void testNavigationToRegister() {
        // Click on register link
        onView(withId(R.id.textViewRegister)).perform(click());
        
        // Verify navigation to RegisterActivity
        // Note: Need to add Espresso-Intents to the project for this
        // intended(hasComponent(RegisterActivity.class.getName()));
    }

    @Test
    public void testNavigationToForgotPassword() {
        // Click on forgot password link
        onView(withId(R.id.textViewForgotPassword)).perform(click());
        
        // Verify navigation to ForgotPasswordActivity or dialog shown
        // This depends on the implementation
    }
    
    /**
     * Custom matcher for TextInputLayout error messages
     */
    private static org.hamcrest.Matcher<android.view.View> hasTextInputError(String expectedError) {
        return new org.hamcrest.TypeSafeMatcher<android.view.View>() {
            @Override
            protected boolean matchesSafely(android.view.View item) {
                if (!(item instanceof com.google.android.material.textfield.TextInputLayout)) {
                    return false;
                }
                
                com.google.android.material.textfield.TextInputLayout textInputLayout = 
                        (com.google.android.material.textfield.TextInputLayout) item;
                
                CharSequence error = textInputLayout.getError();
                return error != null && error.toString().equals(expectedError);
            }
            
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText("with error: " + expectedError);
            }
        };
    }
} 