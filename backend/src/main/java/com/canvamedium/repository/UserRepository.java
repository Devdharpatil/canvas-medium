package com.canvamedium.repository;

import com.canvamedium.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by their username.
     *
     * @param username The username to search for
     * @return An Optional containing the found user, or empty if not found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by their email.
     *
     * @param email The email to search for
     * @return An Optional containing the found user, or empty if not found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a username exists.
     *
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if an email exists.
     *
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by full name containing the given text (case-insensitive).
     *
     * @param fullName The full name to search for
     * @param pageable Pagination information
     * @return A page of users matching the search criteria
     */
    Page<User> findByFullNameContainingIgnoreCase(String fullName, Pageable pageable);
    
    /**
     * Find users by username containing the given text (case-insensitive).
     *
     * @param username The username to search for
     * @param pageable Pagination information
     * @return A page of users matching the search criteria
     */
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    /**
     * Find users by their enabled status.
     *
     * @param enabled The enabled status to search for
     * @param pageable Pagination information
     * @return A page of users matching the search criteria
     */
    Page<User> findByEnabled(boolean enabled, Pageable pageable);
    
    /**
     * Find users by their email verification status.
     *
     * @param emailVerified The email verification status to search for
     * @param pageable Pagination information
     * @return A page of users matching the search criteria
     */
    Page<User> findByEmailVerified(boolean emailVerified, Pageable pageable);
} 