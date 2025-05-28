package com.canvamedium.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an article in the CanvaMedium application.
 * Articles are created using templates and contain the actual content.
 */
@Entity
@Table(name = "article")
public class Article {
    
    /**
     * Enum defining the possible status values for an article.
     */
    public enum Status {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Article title is required")
    @Size(max = 200, message = "Title cannot be longer than 200 characters")
    @Column(name = "title", nullable = false)
    private String title;
    
    @NotNull(message = "Article content is required")
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode content;
    
    @Size(max = 500, message = "Preview text cannot be longer than 500 characters")
    @Column(name = "preview_text")
    private String previewText;
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private Template template;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "article_categories",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "article_tags",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.DRAFT;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "featured", nullable = false)
    private boolean featured = false;
    
    /**
     * Default constructor for JPA.
     */
    public Article() {
    }
    
    /**
     * Constructor with required fields.
     *
     * @param title       The title of the article
     * @param content     The content of the article as a JSON structure
     * @param previewText The preview text for the article
     * @param thumbnailUrl The URL of the article thumbnail image
     * @param template    The template used for the article
     */
    public Article(String title, JsonNode content, String previewText, String thumbnailUrl, Template template) {
        this.title = title;
        this.content = content;
        this.previewText = previewText;
        this.thumbnailUrl = thumbnailUrl;
        this.template = template;
        this.status = Status.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with required fields and status.
     *
     * @param title       The title of the article
     * @param content     The content of the article as a JSON structure
     * @param previewText The preview text for the article
     * @param thumbnailUrl The URL of the article thumbnail image
     * @param template    The template used for the article
     * @param status      The status of the article
     */
    public Article(String title, JsonNode content, String previewText, String thumbnailUrl, Template template, Status status) {
        this.title = title;
        this.content = content;
        this.previewText = previewText;
        this.thumbnailUrl = thumbnailUrl;
        this.template = template;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (status == Status.PUBLISHED) {
            this.publishedAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    
    /**
     * Gets the article ID.
     *
     * @return The article ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Sets the article ID.
     *
     * @param id The article ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * Gets the article title.
     *
     * @return The article title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the article title.
     *
     * @param title The article title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the article content.
     *
     * @return The article content as a JSON structure
     */
    public JsonNode getContent() {
        return content;
    }
    
    /**
     * Sets the article content.
     *
     * @param content The article content to set as a JSON structure
     */
    public void setContent(JsonNode content) {
        this.content = content;
    }
    
    /**
     * Gets the article preview text.
     *
     * @return The article preview text
     */
    public String getPreviewText() {
        return previewText;
    }
    
    /**
     * Sets the article preview text.
     *
     * @param previewText The article preview text to set
     */
    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }
    
    /**
     * Gets the article thumbnail URL.
     *
     * @return The article thumbnail URL
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    /**
     * Sets the article thumbnail URL.
     *
     * @param thumbnailUrl The article thumbnail URL to set
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    /**
     * Gets the template used for the article.
     *
     * @return The template used for the article
     */
    public Template getTemplate() {
        return template;
    }
    
    /**
     * Sets the template used for the article.
     *
     * @param template The template to set
     */
    public void setTemplate(Template template) {
        this.template = template;
    }
    
    /**
     * Gets the categories associated with this article.
     *
     * @return The set of categories
     */
    public Set<Category> getCategories() {
        return categories;
    }
    
    /**
     * Sets the categories associated with this article.
     *
     * @param categories The set of categories to set
     */
    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }
    
    /**
     * Adds a category to this article.
     *
     * @param category The category to add
     */
    public void addCategory(Category category) {
        this.categories.add(category);
    }
    
    /**
     * Removes a category from this article.
     *
     * @param category The category to remove
     */
    public void removeCategory(Category category) {
        this.categories.remove(category);
    }
    
    /**
     * Gets the tags associated with this article.
     *
     * @return The set of tags
     */
    public Set<Tag> getTags() {
        return tags;
    }
    
    /**
     * Sets the tags associated with this article.
     *
     * @param tags The set of tags to set
     */
    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
    
    /**
     * Adds a tag to this article.
     *
     * @param tag The tag to add
     */
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }
    
    /**
     * Removes a tag from this article.
     *
     * @param tag The tag to remove
     */
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }
    
    /**
     * Gets the article status.
     *
     * @return The article status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Sets the article status.
     *
     * @param status The article status to set
     */
    public void setStatus(Status status) {
        this.status = status;
        
        // Set publishedAt if article is being published for the first time
        if (status == Status.PUBLISHED && this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Gets the article publication timestamp.
     *
     * @return The publication timestamp
     */
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    /**
     * Sets the article publication timestamp.
     *
     * @param publishedAt The publication timestamp to set
     */
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    /**
     * Gets the article creation timestamp.
     *
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the article creation timestamp.
     *
     * @param createdAt The creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Gets the article last update timestamp.
     *
     * @return The last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Sets the article last update timestamp.
     *
     * @param updatedAt The last update timestamp to set
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Checks if the article is featured.
     *
     * @return true if the article is featured, false otherwise
     */
    public boolean isFeatured() {
        return featured;
    }
    
    /**
     * Sets whether the article is featured.
     *
     * @param featured The featured status to set
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
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
    
    /**
     * Publishes this article if it is currently in DRAFT status.
     */
    public void publish() {
        if (this.status == Status.DRAFT) {
            this.status = Status.PUBLISHED;
            this.publishedAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Archives this article.
     */
    public void archive() {
        if (this.status != Status.ARCHIVED) {
            this.status = Status.ARCHIVED;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Creates a draft copy of this article.
     *
     * @return A new draft article with the same content
     */
    public Article createDraftCopy() {
        Article copy = new Article();
        copy.setTitle(this.title + " (Draft)");
        copy.setContent(this.content);
        copy.setPreviewText(this.previewText);
        copy.setThumbnailUrl(this.thumbnailUrl);
        copy.setTemplate(this.template);
        copy.setStatus(Status.DRAFT);
        copy.setCreatedAt(LocalDateTime.now());
        copy.setUpdatedAt(LocalDateTime.now());
        return copy;
    }
} 