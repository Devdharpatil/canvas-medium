package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Model class representing a user in the CanvaMedium Android app.
 */
public class User implements Serializable {

    @SerializedName("id")
    private Long id;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("bio")
    private String bio;
    
    @SerializedName("profile_image_url")
    private String profileImageUrl;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("article_count")
    private int articleCount;
    
    /**
     * Default constructor.
     */
    public User() {
    }
    
    /**
     * Constructor with required fields.
     *
     * @param username The username
     * @param email The email address
     * @param name The full name
     */
    public User(String username, String email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
    }
    
    /**
     * Creates a User instance from a Map.
     * This is useful when parsing API responses.
     *
     * @param map The map containing user data
     * @return A new User instance
     */
    public static User fromMap(Map<?, ?> map) {
        User user = new User();
        
        if (map.containsKey("id")) {
            Object idObj = map.get("id");
            if (idObj instanceof Number) {
                user.setId(((Number) idObj).longValue());
            }
        }
        
        if (map.containsKey("username")) {
            Object usernameObj = map.get("username");
            if (usernameObj != null) {
                user.setUsername(usernameObj.toString());
            }
        }
        
        if (map.containsKey("email")) {
            Object emailObj = map.get("email");
            if (emailObj != null) {
                user.setEmail(emailObj.toString());
            }
        }
        
        if (map.containsKey("name")) {
            Object nameObj = map.get("name");
            if (nameObj != null) {
                user.setName(nameObj.toString());
            }
        }
        
        if (map.containsKey("bio")) {
            Object bioObj = map.get("bio");
            if (bioObj != null) {
                user.setBio(bioObj.toString());
            }
        }
        
        if (map.containsKey("profile_image_url")) {
            Object imageUrlObj = map.get("profile_image_url");
            if (imageUrlObj != null) {
                user.setProfileImageUrl(imageUrlObj.toString());
            }
        }
        
        if (map.containsKey("created_at")) {
            Object createdAtObj = map.get("created_at");
            if (createdAtObj != null) {
                user.setCreatedAt(createdAtObj.toString());
            }
        }
        
        if (map.containsKey("updated_at")) {
            Object updatedAtObj = map.get("updated_at");
            if (updatedAtObj != null) {
                user.setUpdatedAt(updatedAtObj.toString());
            }
        }
        
        if (map.containsKey("role")) {
            Object roleObj = map.get("role");
            if (roleObj != null) {
                user.setRole(roleObj.toString());
            }
        }
        
        if (map.containsKey("article_count")) {
            Object countObj = map.get("article_count");
            if (countObj instanceof Number) {
                user.setArticleCount(((Number) countObj).intValue());
            }
        }
        
        return user;
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
     * Gets the email address.
     *
     * @return The email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets the email address.
     *
     * @param email The email address
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * Gets the full name.
     *
     * @return The full name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the full name.
     *
     * @param name The full name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the user bio.
     *
     * @return The user bio
     */
    public String getBio() {
        return bio;
    }
    
    /**
     * Sets the user bio.
     *
     * @param bio The user bio
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
     * Gets the creation timestamp.
     *
     * @return The creation timestamp
     */
    public String getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the creation timestamp.
     *
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     *
     * @return The last update timestamp
     */
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt The last update timestamp
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Gets the user role.
     *
     * @return The user role
     */
    public String getRole() {
        return role;
    }
    
    /**
     * Sets the user role.
     *
     * @param role The user role
     */
    public void setRole(String role) {
        this.role = role;
    }
    
    /**
     * Gets the number of articles authored by this user.
     *
     * @return The article count
     */
    public int getArticleCount() {
        return articleCount;
    }
    
    /**
     * Sets the number of articles authored by this user.
     *
     * @param articleCount The article count
     */
    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
} 