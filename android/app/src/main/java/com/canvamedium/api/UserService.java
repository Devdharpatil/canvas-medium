package com.canvamedium.api;

import com.canvamedium.model.SettingsUpdateRequest;
import com.canvamedium.model.UserProfile;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Body;
import retrofit2.http.Path;

/**
 * Retrofit service interface for user-related API endpoints.
 */
public interface UserService {

    /**
     * Get the current user's profile.
     *
     * @return Call object with user profile
     */
    @GET("api/users/profile")
    Call<UserProfile> getCurrentUserProfile();

    /**
     * Get a specific user's profile by ID.
     *
     * @param userId The ID of the user
     * @return Call object with user profile
     */
    @GET("api/users/{userId}")
    Call<UserProfile> getUserProfile(@Path("userId") Long userId);

    /**
     * Get a specific user's profile by username.
     *
     * @param username The username of the user
     * @return Call object with user profile
     */
    @GET("api/users/username/{username}")
    Call<UserProfile> getUserProfileByUsername(@Path("username") String username);

    /**
     * Update the current user's profile.
     *
     * @param userProfile The updated profile information
     * @return Call object with updated user profile
     */
    @PUT("api/users/profile")
    Call<UserProfile> updateUserProfile(@Body UserProfile userProfile);
    
    /**
     * Update user settings.
     *
     * @param request The settings update request
     * @return Call object with updated user profile
     */
    @PUT("api/users/settings")
    Call<UserProfile> updateSettings(@Body SettingsUpdateRequest request);
    
    /**
     * Change user password.
     *
     * @param request The settings update request containing password information
     * @return Call object with Void response
     */
    @PUT("api/users/password")
    Call<Void> changePassword(@Body SettingsUpdateRequest request);
} 