package com.canvamedium.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user in the CanvaMedium application.
 * Users can create and manage articles, templates, and have roles for authorization.
 */
@Entity
@Table(name = "users") // Using "users" instead of "user" which is a reserved keyword in some DBs
public class User {
    
    /**
     * Enum defining the possible roles for a user.
     */
    public enum Role {
        ROLE_USER,
        ROLE_EDITOR,
        ROLE_ADMIN
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "password", nullable = false)
    private String password;
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot be longer than 100 characters")
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Size(max = 500, message = "Bio cannot be longer than 500 characters")
    @Column(name = "bio")
    private String bio;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    /**
     * Default constructor for JPA.
     */
    public User() {
    }
    
    /**
     * Constructor with required fields.
     *
     * @param username   The username of the user
     * @param email      The email of the user
     * @param password   The password of the user (encrypted)
     * @param fullName   The full name of the user
     */
    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.roles.add(Role.ROLE_USER); // Default role
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
     * @param id The user ID to set
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
     * @param username The username to set
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
     * @param email The email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Gets the password.
     *
     * @return The password (encrypted)
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password.
     *
     * @param password The password to set (should be encrypted)
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Gets the full name.
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }
    
    /**
     * Sets the full name.
     *
     * @param fullName The full name to set
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
     * @param bio The bio to set
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
     * @param profileImageUrl The profile image URL to set
     */
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    
    /**
     * Gets the user roles.
     *
     * @return The set of roles
     */
    public Set<Role> getRoles() {
        return roles;
    }
    
    /**
     * Sets the user roles.
     *
     * @param roles The set of roles to set
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    /**
     * Adds a role to the user.
     *
     * @param role The role to add
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    /**
     * Removes a role from the user.
     *
     * @param role The role to remove
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    /**
     * Checks if the user has a specific role.
     *
     * @param role The role to check
     * @return true if the user has the role, false otherwise
     */
    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }
    
    /**
     * Gets whether the user is enabled.
     *
     * @return true if the user is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether the user is enabled.
     *
     * @param enabled The enabled status to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Gets whether the user's email is verified.
     *
     * @return true if the email is verified, false otherwise
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }
    
    /**
     * Sets whether the user's email is verified.
     *
     * @param emailVerified The email verified status to set
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the creation timestamp.
     *
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Gets the update timestamp.
     *
     * @return The update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Sets the update timestamp.
     *
     * @param updatedAt The update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Gets the last login timestamp.
     *
     * @return The last login timestamp
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    /**
     * Sets the last login timestamp.
     *
     * @param lastLoginAt The last login timestamp to set
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    /**
     * Updates the last login timestamp to the current time.
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
    
    /**
     * Pre-persist hook to set creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Pre-update hook to update the update timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 