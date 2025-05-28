package com.canvamedium.service.impl;

import com.canvamedium.model.User;
import com.canvamedium.model.User.Role;
import com.canvamedium.repository.UserRepository;
import com.canvamedium.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the UserService interface.
 */
@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Constructor with dependencies injection.
     *
     * @param userRepository  The user repository
     * @param passwordEncoder The password encoder
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find user by username first
        Optional<User> userOptional = userRepository.findByUsername(usernameOrEmail);
        
        // If not found by username, try by email
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(usernameOrEmail);
        }
        
        // If still not found, throw exception
        User user = userOptional.orElseThrow(() -> 
            new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, // account non-expired
                true, // credentials non-expired
                true, // account non-locked
                authorities
        );
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    @Transactional
    public User registerUser(User user) {
        // Validate username and email uniqueness
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Encrypt the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role if not set
        if (user.getRoles().isEmpty()) {
            user.addRole(Role.ROLE_USER);
        }
        
        // Set timestamps and default values
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(true);
        user.setEmailVerified(false); // Email verification should be handled separately
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        // Update only non-sensitive fields
        user.setFullName(userDetails.getFullName());
        user.setBio(userDetails.getBio());
        user.setProfileImageUrl(userDetails.getProfileImageUrl());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password");
        }
        
        // Encrypt and set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        userRepository.delete(user);
    }
    
    @Override
    @Transactional
    public User setUserEnabled(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User setEmailVerified(Long id, boolean emailVerified) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        user.setEmailVerified(emailVerified);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User addRoleToUser(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        user.addRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User removeRoleFromUser(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // Don't remove the last role
        if (user.getRoles().size() <= 1) {
            throw new IllegalStateException("Cannot remove the last role from a user");
        }
        
        user.removeRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void recordLogin(String usernameOrEmail) {
        // Try to find user by username first
        Optional<User> userOptional = userRepository.findByUsername(usernameOrEmail);
        
        // If not found by username, try by email
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByEmail(usernameOrEmail);
        }
        
        // If still not found, throw exception
        User user = userOptional.orElseThrow(() -> 
            new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        
        user.updateLastLogin();
        userRepository.save(user);
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
} 