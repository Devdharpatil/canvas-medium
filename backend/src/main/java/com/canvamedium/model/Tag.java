package com.canvamedium.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a tag for articles.
 */
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 50, message = "Tag name must be between 2 and 50 characters")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @ManyToMany(mappedBy = "tags")
    private Set<Article> articles = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Default constructor for JPA.
     */
    public Tag() {
    }

    /**
     * Constructor with required fields.
     *
     * @param name The name of the tag
     * @param slug The URL-friendly slug for the tag
     */
    public Tag(String name, String slug) {
        this.name = name;
        this.slug = slug;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the tag ID.
     *
     * @return The tag ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the tag ID.
     *
     * @param id The tag ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the tag name.
     *
     * @return The tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the tag name.
     *
     * @param name The tag name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the tag slug.
     *
     * @return The tag slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Sets the tag slug.
     *
     * @param slug The tag slug to set
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Gets the articles associated with this tag.
     *
     * @return The set of articles
     */
    public Set<Article> getArticles() {
        return articles;
    }

    /**
     * Sets the articles associated with this tag.
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