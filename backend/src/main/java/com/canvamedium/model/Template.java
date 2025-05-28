package com.canvamedium.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Entity representing a template in the CanvaMedium application.
 * Templates are reusable layout structures that can be used to create articles.
 */
@Entity
@Table(name = "template")
public class Template {
    
    /**
     * Element types supported in templates
     */
    public static final String ELEMENT_TYPE_TEXT = "TEXT";
    public static final String ELEMENT_TYPE_IMAGE = "IMAGE";
    public static final String ELEMENT_TYPE_HEADER = "HEADER";
    public static final String ELEMENT_TYPE_DIVIDER = "DIVIDER";
    public static final String ELEMENT_TYPE_QUOTE = "QUOTE";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Template name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @NotNull(message = "Template layout is required")
    @Column(name = "layout", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode layout;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "version")
    private Integer version = 1;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    /**
     * Default constructor for JPA.
     */
    public Template() {
    }
    
    /**
     * Constructor with required fields.
     *
     * @param name   The name of the template
     * @param layout The layout of the template as a JSON structure
     */
    public Template(String name, JsonNode layout) {
        this.name = name;
        this.layout = layout;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
    public Template(String name, String description, JsonNode layout, String thumbnailUrl) {
        this.name = name;
        this.description = description;
        this.layout = layout;
        this.thumbnailUrl = thumbnailUrl;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.version = 1;
    }
    
    // Getters and Setters
    
    /**
     * Gets the template ID.
     *
     * @return The template ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Sets the template ID.
     *
     * @param id The template ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the template name.
     *
     * @return The template name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the template name.
     *
     * @param name The template name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the template description.
     *
     * @return The template description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the template description.
     *
     * @param description The template description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the template layout.
     *
     * @return The template layout as a JSON structure
     */
    public JsonNode getLayout() {
        return layout;
    }
    
    /**
     * Sets the template layout.
     *
     * @param layout The template layout to set as a JSON structure
     */
    public void setLayout(JsonNode layout) {
        this.layout = layout;
    }
    
    /**
     * Gets the template creation timestamp.
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the template creation timestamp.
     *
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Gets the template last update timestamp.
     *
     * @return The last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Sets the template last update timestamp.
     *
     * @param updatedAt The last update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Gets the template version.
     *
     * @return The template version
     */
    public Integer getVersion() {
        return version;
    }
    
    /**
     * Sets the template version.
     *
     * @param version The template version to set
     */
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    /**
     * Gets the thumbnail URL for this template.
     *
     * @return The thumbnail URL
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    /**
     * Sets the thumbnail URL for this template.
     *
     * @param thumbnailUrl The thumbnail URL to set
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
} 