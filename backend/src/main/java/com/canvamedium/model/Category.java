package com.canvamedium.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a category for organizing articles.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Size(max = 255, message = "Description cannot be longer than 255 characters")
    @Column(name = "description")
    private String description;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "icon")
    private String icon;

    @Column(name = "color")
    private String color;

    @Column(name = "is_featured")
    private boolean featured = false;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToMany(mappedBy = "categories")
    private Set<Article> articles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JPA.
     */
    public Category() {
    }

    /**
     * Constructor with required fields.
     *
     * @param name The name of the category
     * @param slug The URL-friendly slug for the category
     */
    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the category ID.
     *
     * @return The category ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the category ID.
     *
     * @param id The category ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the category name.
     *
     * @return The category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the category name.
     *
     * @param name The category name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the category description.
     *
     * @return The category description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the category description.
     *
     * @param description The category description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the category slug.
     *
     * @return The category slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Sets the category slug.
     *
     * @param slug The category slug to set
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Gets the category icon.
     *
     * @return The category icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the category icon.
     *
     * @param icon The category icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Gets the category color.
     *
     * @return The category color
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the category color.
     *
     * @param color The category color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Checks if the category is featured.
     *
     * @return true if the category is featured, false otherwise
     */
    public boolean isFeatured() {
        return featured;
    }

    /**
     * Sets whether the category is featured.
     *
     * @param featured The featured status to set
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    /**
     * Gets the parent category ID.
     *
     * @return The parent category ID, or null if this is a top-level category
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * Sets the parent category ID.
     *
     * @param parentId The parent category ID to set
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * Gets the articles in this category.
     *
     * @return The set of articles
     */
    public Set<Article> getArticles() {
        return articles;
    }

    /**
     * Sets the articles in this category.
     *
     * @param articles The set of articles to set
     */
    public void setArticles(Set<Article> articles) {
        this.articles = articles;
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