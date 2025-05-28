package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a user profile.
 */
public class UserProfile {

    @SerializedName("id")
    private Long id;

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("bio")
    private String bio;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;
    
    @SerializedName("roles")
    private List<String> roles;
    
    @SerializedName("articleCount")
    private int articleCount;
    
    @SerializedName("draftCount")
    private int draftCount;
    
    @SerializedName("templateCount")
    private int templateCount;
    
    @SerializedName("emailVerified")
    private boolean emailVerified;
    
    @SerializedName("joinDate")
    private String joinDate;
    
    @SerializedName("lastLoginDate")
    private String lastLoginDate;

    @SerializedName("notificationsEnabled")
    private boolean notificationsEnabled;
    
    @SerializedName("emailUpdatesEnabled")
    private boolean emailUpdatesEnabled;

    /**
     * Default constructor.
     */
    public UserProfile() {
        roles = new ArrayList<>();
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id The user ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email.
     *
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     *
     * @param email The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's full name.
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the user's full name.
     *
     * @param fullName The full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the bio.
     *
     * @return The bio
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the bio.
     *
     * @param bio The bio
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Gets the profile image URL.
     *
     * @return The profile image URL
     */
    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    /**
     * Sets the profile image URL.
     *
     * @param profileImageUrl The profile image URL
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    /**
     * Gets the user roles.
     *
     * @return The roles
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Sets the user roles.
     *
     * @param roles The roles
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Gets the number of articles published by the user.
     *
     * @return The article count
     */
    public int getArticleCount() {
        return articleCount;
    }

    /**
     * Sets the number of articles published by the user.
     *
     * @param articleCount The article count
     */
    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }

    /**
     * Gets the number of draft articles by the user.
     *
     * @return The draft count
     */
    public int getDraftCount() {
        return draftCount;
    }

    /**
     * Sets the number of draft articles by the user.
     *
     * @param draftCount The draft count
     */
    public void setDraftCount(int draftCount) {
        this.draftCount = draftCount;
    }

    /**
     * Gets the number of templates created by the user.
     *
     * @return The template count
     */
    public int getTemplateCount() {
        return templateCount;
    }

    /**
     * Sets the number of templates created by the user.
     *
     * @param templateCount The template count
     */
    public void setTemplateCount(int templateCount) {
        this.templateCount = templateCount;
    }

    /**
     * Checks if the user's email is verified.
     *
     * @return true if verified, false otherwise
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Sets whether the user's email is verified.
     *
     * @param emailVerified Whether the email is verified
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * Gets the date the user joined.
     *
     * @return The join date as a string
     */
    public String getJoinDate() {
        return joinDate;
    }

    /**
     * Sets the date the user joined.
     *
     * @param joinDate The join date as a string
     */
    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    /**
     * Gets the date the user last logged in.
     *
     * @return The last login date as a string
     */
    public String getLastLoginDate() {
        return lastLoginDate;
    }

    /**
     * Sets the date the user last logged in.
     *
     * @param lastLoginDate The last login date as a string
     */
    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    /**
     * Checks if notifications are enabled for the user.
     * 
     * @return true if notifications are enabled, false otherwise
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets whether notifications are enabled for the user.
     * 
     * @param notificationsEnabled true to enable notifications, false otherwise
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Checks if email updates are enabled for the user.
     * 
     * @return true if email updates are enabled, false otherwise
     */
    public boolean isEmailUpdatesEnabled() {
        return emailUpdatesEnabled;
    }

    /**
     * Sets whether email updates are enabled for the user.
     * 
     * @param emailUpdatesEnabled true to enable email updates, false otherwise
     */
    public void setEmailUpdatesEnabled(boolean emailUpdatesEnabled) {
        this.emailUpdatesEnabled = emailUpdatesEnabled;
    }
} 