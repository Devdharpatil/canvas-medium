package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Model class representing a generic content item in the CanvaMedium Android app.
 */
public class Content implements Serializable {

    @SerializedName("id")
    private Long id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    @SerializedName("author")
    private User author;
    
    /**
     * Default constructor.
     */
    public Content() {
    }
    
    /**
     * Constructor with required fields.
     *
     * @param title       The content title
     * @param description The content description
     * @param content     The content text
     */
    public Content(String title, String description, String content) {
        this.title = title;
        this.description = description;
        this.content = content;
    }
    
    /**
     * Creates a Content instance from a Map.
     * This is useful when parsing API responses.
     *
     * @param map The map containing content data
     * @return A new Content instance
     */
    public static Content fromMap(Map<?, ?> map) {
        Content content = new Content();
        
        if (map.containsKey("id")) {
            Object idObj = map.get("id");
            if (idObj instanceof Number) {
                content.setId(((Number) idObj).longValue());
            }
        }
        
        if (map.containsKey("title")) {
            Object titleObj = map.get("title");
            if (titleObj != null) {
                content.setTitle(titleObj.toString());
            }
        }
        
        if (map.containsKey("description")) {
            Object descObj = map.get("description");
            if (descObj != null) {
                content.setDescription(descObj.toString());
            }
        }
        
        if (map.containsKey("content")) {
            Object contentObj = map.get("content");
            if (contentObj != null) {
                content.setContent(contentObj.toString());
            }
        }
        
        if (map.containsKey("created_at")) {
            Object createdAtObj = map.get("created_at");
            if (createdAtObj != null) {
                content.setCreatedAt(createdAtObj.toString());
            }
        }
        
        if (map.containsKey("updated_at")) {
            Object updatedAtObj = map.get("updated_at");
            if (updatedAtObj != null) {
                content.setUpdatedAt(updatedAtObj.toString());
            }
        }
        
        if (map.containsKey("author") && map.get("author") instanceof Map) {
            Map<?, ?> authorMap = (Map<?, ?>) map.get("author");
            User author = User.fromMap(authorMap);
            content.setAuthor(author);
        }
        
        return content;
    }
    
    /**
     * Gets the content ID.
     *
     * @return The content ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Sets the content ID.
     *
     * @param id The content ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the content title.
     *
     * @return The content title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the content title.
     *
     * @param title The content title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the content description.
     *
     * @return The content description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the content description.
     *
     * @param description The content description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the content text.
     *
     * @return The content text
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Sets the content text.
     *
     * @param content The content text
     */
    public void setContent(String content) {
        this.content = content;
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
     * Gets the author of the content.
     *
     * @return The author
     */
    public User getAuthor() {
        return author;
    }
    
    /**
     * Sets the author of the content.
     *
     * @param author The author
     */
    public void setAuthor(User author) {
        this.author = author;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Content content = (Content) o;
        return Objects.equals(id, content.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Content{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
} 