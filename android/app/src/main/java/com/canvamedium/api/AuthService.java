package com.canvamedium.api;

import com.canvamedium.model.LoginRequest;
import com.canvamedium.model.LoginResponse;
import com.canvamedium.model.RefreshTokenRequest;
import com.canvamedium.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Retrofit service interface for authentication API endpoints.
 */
public interface AuthService {

    /**
     * Login with email and password.
     *
     * @param loginRequest The login credentials
     * @return Call object with login response
     */
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    /**
     * Register a new user.
     *
     * @param registerRequest The registration data
     * @return Call object with registration response
     */
    @POST("api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest registerRequest);

    /**
     * Refresh an expired authentication token.
     *
     * @param refreshTokenRequest The refresh token request
     * @return Call object with new tokens
     */
    @POST("api/auth/refresh")
    Call<LoginResponse> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    /**
     * Check if a username is available.
     *
     * @param username The username to check
     * @return Call object with availability response
     */
    @GET("api/auth/check-username/{username}")
    Call<AvailabilityResponse> checkUsernameAvailability(@Path("username") String username);

    /**
     * Check if an email is available.
     *
     * @param email The email to check
     * @return Call object with availability response
     */
    @GET("api/auth/check-email/{email}")
    Call<AvailabilityResponse> checkEmailAvailability(@Path("email") String email);

    /**
     * Response class for availability checks.
     */
    class AvailabilityResponse {
        private boolean available;

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }
    }
} 