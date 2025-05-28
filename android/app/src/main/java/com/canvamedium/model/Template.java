package com.canvamedium.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

/**
 * Model class representing a Template in the CanvaMedium Android app.
 * Templates are reusable layout structures that can be used to create articles.
 */
public class Template implements Serializable {
    
    /**
     * Element types supported in templates
     */
    public static final String ELEMENT_TYPE_TEXT = "TEXT";
    public static final String ELEMENT_TYPE_IMAGE = "IMAGE";
    public static final String ELEMENT_TYPE_HEADER = "HEADER";
    public static final String ELEMENT_TYPE_DIVIDER = "DIVIDER";
    public static final String ELEMENT_TYPE_QUOTE = "QUOTE";
    
    private Long id;
    
    private String name;
    
    private String description;
    
    private JsonObject layout;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    private Integer version;
    
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    
    /**
     * Default constructor for Retrofit serialization.
     */
    public Template() {
    }
    
    /**
     * Constructor with required fields.
     *
     * @param name   The name of the template
     * @param layout The layout of the template as a JSON structure
     */
    public Template(String name, JsonObject layout) {
        this.name = name;
        this.layout = layout;
        this.version = 1;
    }
    
    /**
     * Constructor with all fields.
     *
     * @param name        The name of the template
     * @param description The description of the template
     * @param layout      The layout of the template as a JSON structure
     * @param thumbnailUrl The URL of the template thumbnail image
     */
    public Template(String name, String description, JsonObject layout, String thumbnailUrl) {
        this.name = name;
        this.description = description;
        this.layout = layout;
        this.thumbnailUrl = thumbnailUrl;
        this.version = 1;
    }
    
    /**
     * Creates a Template instance from a Map.
     * This is useful when parsing API responses.
     *
     * @param map The map containing template data
     * @return A new Template instance
     */
    public static Template fromMap(Map<?, ?> map) {
        Template template = new Template();
        
        // Extract and set the template properties from the map
        if (map.containsKey("id")) {
            Object idObj = map.get("id");
            if (idObj instanceof Number) {
                template.setId(((Number) idObj).longValue());
            }
        }
        
        if (map.containsKey("name")) {
            Object nameObj = map.get("name");
            if (nameObj != null) {
                template.setName(nameObj.toString());
            }
        }
        
        if (map.containsKey("description")) {
            Object descObj = map.get("description");
            if (descObj != null) {
                template.setDescription(descObj.toString());
            }
        }
        
        if (map.containsKey("layout")) {
            Object layoutObj = map.get("layout");
            if (layoutObj != null) {
                Gson gson = new Gson();
                template.setLayout(gson.toJsonTree(layoutObj).getAsJsonObject());
            }
        }
        
        if (map.containsKey("created_at")) {
            Object createdAtObj = map.get("created_at");
            if (createdAtObj != null) {
                template.setCreatedAt(createdAtObj.toString());
            }
        }
        
        if (map.containsKey("updated_at")) {
            Object updatedAtObj = map.get("updated_at");
            if (updatedAtObj != null) {
                template.setUpdatedAt(updatedAtObj.toString());
            }
        }
        
        if (map.containsKey("version")) {
            Object versionObj = map.get("version");
            if (versionObj instanceof Number) {
                template.setVersion(((Number) versionObj).intValue());
            }
        }
        
        if (map.containsKey("thumbnail_url")) {
            Object thumbnailObj = map.get("thumbnail_url");
            if (thumbnailObj != null) {
                template.setThumbnailUrl(thumbnailObj.toString());
            }
        }
        
        return template;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public JsonObject getLayout() {
        return layout;
    }
    
    public void setLayout(JsonObject layout) {
        this.layout = layout;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    @Override
    public String toString() {
        return name;
    }
} 