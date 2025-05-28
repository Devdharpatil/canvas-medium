package com.canvamedium.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.canvamedium.db.converter.DateConverter;
import com.canvamedium.db.converter.StringListConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entity class for Article data stored in Room database
 */
@Entity(
    tableName = "articles",
    indices = {
        @Index(value = {"created_at"}),
        @Index(value = {"status"}),
        @Index(value = {"title"}),
        @Index(value = {"category_id"}),
        @Index(value = {"author_id"}),
        @Index(value = {"is_bookmarked"}),
        @Index(value = {"is_synced"}),
        @Index(value = {"published_at"})
    }
)
@TypeConverters({DateConverter.class, StringListConverter.class})
public class ArticleEntity extends BaseEntity {

    @PrimaryKey
    @NonNull
    private Long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "preview_text")
    private String previewText;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "thumbnail_url")
    private String thumbnailUrl;

    @ColumnInfo(name = "author_id")
    private Long authorId;

    @ColumnInfo(name = "author_name")
    private String authorName;

    @ColumnInfo(name = "published_at")
    private Date publishedAt;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "template_id")
    private Long templateId;

    @ColumnInfo(name = "category_id")
    private Long categoryId;

    @ColumnInfo(name = "category_name")
    private String categoryName;

    @ColumnInfo(name = "tags")
    private List<String> tags;

    @ColumnInfo(name = "is_bookmarked")
    private boolean isBookmarked;

    /**
     * Default constructor for Room
     */
    public ArticleEntity() {
        this.tags = new ArrayList<>();
        initAuditFields();
    }

    /**
     * Constructor with all fields
     */
    public ArticleEntity(@NonNull Long id, String title, String previewText, 
                         String content, String thumbnailUrl, Long authorId, 
                         String authorName, Date createdAt, Date updatedAt, 
                         Date publishedAt, String status, Long templateId, 
                         Long categoryId, String categoryName, List<String> tags, 
                         boolean isBookmarked) {
        this.id = id;
        this.title = title;
        this.previewText = previewText;
        this.content = content;
        this.thumbnailUrl = thumbnailUrl;
        this.authorId = authorId;
        this.authorName = authorName;
        this.publishedAt = publishedAt;
        this.status = status;
        this.templateId = templateId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.isBookmarked = isBookmarked;
        
        // Set audit fields
        setCreatedAt(createdAt != null ? createdAt : new Date());
        setUpdatedAt(updatedAt != null ? updatedAt : new Date());
        setSynced(true);
        setLastSyncTime(new Date());
    }

    @NonNull
    public Long getId() {
        return id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateAuditFields();
    }

    public String getPreviewText() {
        return previewText;
    }

    public void setPreviewText(String previewText) {
        this.previewText = previewText;
        updateAuditFields();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        updateAuditFields();
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        updateAuditFields();
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
        updateAuditFields();
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
        updateAuditFields();
    }

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
        updateAuditFields();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        updateAuditFields();
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
        updateAuditFields();
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        updateAuditFields();
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
        updateAuditFields();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
        updateAuditFields();
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
        updateAuditFields();
    }
} 