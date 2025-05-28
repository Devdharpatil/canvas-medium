package com.canvamedium.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model class representing an Article in the CanvaMedium Android app.
 */
public class Article implements Serializable {

    /**
     * Article status constants
     */
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    private Long id;
    
    private String title;
    
    private JsonObject content;
    
    @SerializedName("preview_text")
    private String previewText;
    
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    
    @SerializedName("template_id")
    private Long templateId;
    
    private Template template;
    
    private String status;
    
    @SerializedName("published_at")
    private String publishedAt;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    private boolean featured;
    
    @SerializedName("categories")
    private List<Category> categories = new ArrayList<>();
    
    @SerializedName("tags")
    private List<Tag> tags = new ArrayList<>();
    
    @SerializedName("bookmarked")
    private boolean bookmarked;
    
    /**
     * Default constructor for Retrofit serialization.
     */
    public Article() {
        this.status = STATUS_DRAFT; // Default status is DRAFT
    }
    
    /**
     * Constructor with required fields.
     *
     * @param title       The title of the article
     * @param content     The content of the article as a JSON structure
     * @param previewText The preview text for the article
     * @param thumbnailUrl The URL of the article thumbnail image
     * @param templateId  The ID of the template used for the article
     */
    public Article(String title, JsonObject content, String previewText, String thumbnailUrl, Long templateId) {
        this.title = title;
        this.content = content;
        this.previewText = previewText;
        this.thumbnailUrl = thumbnailUrl;
        this.templateId = templateId;
        this.status = STATUS_DRAFT; // Default status is DRAFT
    }
    
    /**
     * Constructor with all fields including status.
     *
     * @param title       The title of the article
     * @param content     The content of the article as a JSON structure
     * @param previewText The preview text for the article
     * @param thumbnailUrl The URL of the article thumbnail image
     * @param templateId  The ID of the template used for the article
     * @param status      The status of the article (DRAFT, PUBLISHED, ARCHIVED)
     */
    public Article(String title, JsonObject content, String previewText, String thumbnailUrl, Long templateId, String status) {
        this.title = title;
        this.content = content;
        this.previewText = previewText;
        this.thumbnailUrl = thumbnailUrl;
        this.templateId = templateId;
        this.status = status;
    }
    
    /**
     * Creates an Article instance from a Map.
     * This is useful when parsing API responses.
     *
     * @param map The map containing article data
     * @return A new Article instance
     */
    public static Article fromMap(Map<?, ?> map) {
        Article article = new Article();
        
        // Extract and set the article properties from the map
        if (map.containsKey("id")) {
            Object idObj = map.get("id");
            if (idObj instanceof Number) {
                article.setId(((Number) idObj).longValue());
            }
        }
        
        if (map.containsKey("title")) {
            Object titleObj = map.get("title");
            if (titleObj != null) {
                article.setTitle(titleObj.toString());
            }
        }
        
        if (map.containsKey("content")) {
            Object contentObj = map.get("content");
            if (contentObj != null) {
                Gson gson = new Gson();
                article.setContent(gson.toJsonTree(contentObj).getAsJsonObject());
            }
        }
        
        if (map.containsKey("preview_text")) {
            Object previewObj = map.get("preview_text");
            if (previewObj != null) {
                article.setPreviewText(previewObj.toString());
            }
        }
        
        if (map.containsKey("thumbnail_url")) {
            Object thumbnailObj = map.get("thumbnail_url");
            if (thumbnailObj != null) {
                article.setThumbnailUrl(thumbnailObj.toString());
            }
        }
        
        if (map.containsKey("template_id")) {
            Object templateIdObj = map.get("template_id");
            if (templateIdObj instanceof Number) {
                article.setTemplateId(((Number) templateIdObj).longValue());
            }
        }
        
        if (map.containsKey("template") && map.get("template") instanceof Map) {
            Map<?, ?> templateMap = (Map<?, ?>) map.get("template");
            Template template = Template.fromMap(templateMap);
            article.setTemplate(template);
        }
        
        if (map.containsKey("status")) {
            Object statusObj = map.get("status");
            if (statusObj != null) {
                article.setStatus(statusObj.toString());
            }
        }
        
        if (map.containsKey("published_at")) {
            Object publishedAtObj = map.get("published_at");
            if (publishedAtObj != null) {
                article.setPublishedAt(publishedAtObj.toString());
            }
        }
        
        if (map.containsKey("created_at")) {
            Object createdAtObj = map.get("created_at");
            if (createdAtObj != null) {
                article.setCreatedAt(createdAtObj.toString());
            }
        }
        
        if (map.containsKey("updated_at")) {
            Object updatedAtObj = map.get("updated_at");
            if (updatedAtObj != null) {
                article.setUpdatedAt(updatedAtObj.toString());
            }
        }
        
        if (map.containsKey("featured")) {
            Object featuredObj = map.get("featured");
            if (featuredObj instanceof Boolean) {
                article.setFeatured((Boolean) featuredObj);
            }
        }
        
        if (map.containsKey("categories")) {
            Object categoriesObj = map.get("categories");
            if (categoriesObj instanceof List) {
                List<?> categoryList = (List<?>) categoriesObj;
                for (Object categoryObj : categoryList) {
                    if (categoryObj instanceof Map) {
                        Map<?, ?> categoryMap = (Map<?, ?>) categoryObj;
                        Category category = Category.fromMap(categoryMap);
                        article.getCategories().add(category);
                    }
                }
            }
        }
        
        if (map.containsKey("tags")) {
            Object tagsObj = map.get("tags");
            if (tagsObj instanceof List) {
                List<?> tagList = (List<?>) tagsObj;
                for (Object tagObj : tagList) {
                    if (tagObj instanceof Map) {
                        Map<?, ?> tagMap = (Map<?, ?>) tagObj;
                        Tag tag = Tag.fromMap(tagMap);
                        article.getTags().add(tag);
                    }
                }
            }
        }
        
        if (map.containsKey("bookmarked")) {
            Object bookmarkedObj = map.get("bookmarked");
            if (bookmarkedObj instanceof Boolean) {
                article.setBookmarked((Boolean) bookmarkedObj);
            }
        }
        
        return article;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public JsonObject getContent() {
        return content;
    }
    
    public void setContent(JsonObject content) {
        this.content = content;
    }
    
    public String getPreviewText() {
        return previewText;
    }
    
    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public Long getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
    
    public Template getTemplate() {
        return template;
    }
    
    public void setTemplate(Template template) {
        this.template = template;
        if (template != null) {
            this.templateId = template.getId();
        }
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
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
    
    public boolean isFeatured() {
        return featured;
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    
    /**
     * Returns a formatted date string for display in the UI.
     *
     * @return Formatted date string
     */
    public String getFormattedDate() {
        // If published, use the published date; otherwise, use the created date
        return publishedAt != null ? publishedAt : createdAt;
    }
    
    /**
     * Checks if the article is a draft.
     *
     * @return True if the article is a draft, false otherwise
     */
    public boolean isDraft() {
        return STATUS_DRAFT.equals(status);
    }
    
    /**
     * Checks if the article is published.
     *
     * @return True if the article is published, false otherwise
     */
    public boolean isPublished() {
        return STATUS_PUBLISHED.equals(status);
    }
    
    /**
     * Checks if the article is archived.
     *
     * @return True if the article is archived, false otherwise
     */
    public boolean isArchived() {
        return STATUS_ARCHIVED.equals(status);
    }
    
    /**
     * Creates a draft copy of this article.
     *
     * @return A new Article instance as a draft copy of this article
     */
    public Article createDraftCopy() {
        Article draft = new Article(
            "Draft of " + this.title,
            this.content,
            this.previewText,
            this.thumbnailUrl,
            this.templateId
        );
        draft.setStatus(STATUS_DRAFT);
        return draft;
    }

    /**
     * Gets the categories associated with this article.
     *
     * @return The list of categories
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * Sets the categories for this article.
     *
     * @param categories The list of categories
     */
    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    /**
     * Gets the tags associated with this article.
     *
     * @return The list of tags
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Sets the tags for this article.
     *
     * @param tags The list of tags
     */
    public void setTags(List<Tag> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    /**
     * Checks if this article is bookmarked by the current user.
     *
     * @return True if bookmarked, false otherwise
     */
    public boolean isBookmarked() {
        return bookmarked;
    }

    /**
     * Sets whether this article is bookmarked by the current user.
     *
     * @param bookmarked The bookmarked status
     */
    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }
} 