package com.canvamedium.service;

import com.canvamedium.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

/**
 * Service interface for User operations.
 */
public interface UserService extends UserDetailsService {
    
    /**
     * Finds a user by their ID.
     *
     * @param id The user ID
     * @return An Optional containing the found user, or empty if not found
     */
    Optional<User> findById(Long id);
    
    /**
     * Finds a user by their username.
     *
     * @param username The username
     * @return An Optional containing the found user, or empty if not found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by their email.
     *
     * @param email The email
     * @return An Optional containing the found user, or empty if not found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Registers a new user in the system.
     *
     * @param user The user to register
     * @return The saved user with roles and encrypted password
     */
    User registerUser(User user);
    
    /**
     * Updates an existing user.
     *
     * @param id   The user ID
     * @param user The updated user information
     * @return The updated user
     */
    User updateUser(Long id, User user);
    
    /**
     * Changes a user's password.
     *
     * @param userId      The user ID
     * @param oldPassword The old password (for verification)
     * @param newPassword The new password
     * @return The updated user
     */
    User changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * Gets all users with pagination.
     *
     * @param pageable Pagination information
     * @return A page of users
     */
    Page<User> findAll(Pageable pageable);
    
    /**
     * Deletes a user.
     *
     * @param id The user ID
     */
    void deleteUser(Long id);
    
    /**
     * Enables or disables a user.
     *
     * @param id      The user ID
     * @param enabled The enabled status to set
     * @return The updated user
     */
    User setUserEnabled(Long id, boolean enabled);
    
    /**
     * Sets a user's email verification status.
     *
     * @param id            The user ID
     * @param emailVerified The email verified status to set
     * @return The updated user
     */
    User setEmailVerified(Long id, boolean emailVerified);
    
    /**
     * Adds a role to a user.
     *
     * @param userId The user ID
     * @param role   The role to add
     * @return The updated user
     */
    User addRoleToUser(Long userId, User.Role role);
    
    /**
     * Removes a role from a user.
     *
     * @param userId The user ID
     * @param role   The role to remove
     * @return The updated user
     */
    User removeRoleFromUser(Long userId, User.Role role);
    
    /**
     * Records a user login.
     *
     * @param usernameOrEmail The username or email of the user who logged in
     */
    void recordLogin(String usernameOrEmail);
    
    /**
     * Checks if a username is available (not already taken).
     *
     * @param username The username to check
     * @return true if the username is available, false otherwise
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Checks if an email is available (not already taken).
     *
     * @param email The email to check
     * @return true if the email is available, false otherwise
     */
    boolean isEmailAvailable(String email);
} 