package com.canvamedium.controller;

import com.canvamedium.model.User;
import com.canvamedium.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

/**
 * Controller for development tools.
 * This controller should be disabled or removed in production.
 */
@Controller
@RequestMapping("/dev-tools")
public class DevToolsController {
    
    private static final Logger logger = LoggerFactory.getLogger(DevToolsController.class);
    
    private final UserService userService;
    
    @Autowired
    public DevToolsController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * Display the password reset form.
     *
     * @return The model and view
     */
    @GetMapping("/reset-password")
    public ModelAndView showResetPasswordForm() {
        return new ModelAndView("dev-reset-password");
    }
    
    /**
     * Reset a user's password.
     *
     * @param email    The email of the user
     * @param password The new password
     * @return The model and view with the result
     */
    @PostMapping("/reset-password")
    public ModelAndView resetPassword(@RequestParam String email, @RequestParam String password) {
        ModelAndView modelAndView = new ModelAndView("dev-reset-password");
        
        try {
            Optional<User> userOptional = userService.findByEmail(email);
            if (userOptional.isEmpty()) {
                modelAndView.addObject("error", "User not found with email: " + email);
                return modelAndView;
            }
            
            User user = userOptional.get();
            String encodedPassword = userService.encodePassword(password);
            user.setPassword(encodedPassword);
            user.setUpdatedAt(java.time.LocalDateTime.now());
            userService.saveUser(user);
            
            logger.info("Password reset for user: {}, email: {}", user.getUsername(), email);
            
            modelAndView.addObject("success", "Password reset successful for user: " + user.getUsername());
        } catch (Exception e) {
            logger.error("Error resetting password: {}", e.getMessage());
            modelAndView.addObject("error", "Error: " + e.getMessage());
        }
        
        return modelAndView;
    }
} 