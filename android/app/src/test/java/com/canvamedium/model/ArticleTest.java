package com.canvamedium.model;

import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for the {@link Article} class.
 */
public class ArticleTest {
    
    private static final String TEST_TITLE = "Test Article";
    private static final String TEST_PREVIEW = "This is a test preview";
    private static final String TEST_THUMBNAIL = "https://example.com/image.jpg";
    private static final Long TEST_TEMPLATE_ID = 1L;
    
    private JsonObject testContent;
    private Article article;
    
    @Before
    public void setUp() {
        // Create test content
        testContent = new JsonObject();
        testContent.addProperty("text", "Test content text");
        
        // Create article with required fields
        article = new Article(TEST_TITLE, testContent, TEST_PREVIEW, TEST_THUMBNAIL, TEST_TEMPLATE_ID);
    }
    
    @Test
    public void constructor_withRequiredFields_setsFieldsCorrectly() {
        assertEquals("Title should match constructor param", TEST_TITLE, article.getTitle());
        assertEquals("Content should match constructor param", testContent, article.getContent());
        assertEquals("Preview should match constructor param", TEST_PREVIEW, article.getPreviewText());
        assertEquals("Thumbnail should match constructor param", TEST_THUMBNAIL, article.getThumbnailUrl());
        assertEquals("Template ID should match constructor param", TEST_TEMPLATE_ID, article.getTemplateId());
    }
    
    @Test
    public void defaultConstructor_createsEmptyArticle() {
        Article emptyArticle = new Article();
        
        assertNull("Title should be null", emptyArticle.getTitle());
        assertNull("Content should be null", emptyArticle.getContent());
        assertNull("Preview should be null", emptyArticle.getPreviewText());
        assertNull("Thumbnail should be null", emptyArticle.getThumbnailUrl());
        assertNull("Template ID should be null", emptyArticle.getTemplateId());
    }
    
    @Test
    public void setAndGetId_worksCorrectly() {
        Long id = 123L;
        article.setId(id);
        
        assertEquals("ID should be set correctly", id, article.getId());
    }
    
    @Test
    public void setAndGetTitle_worksCorrectly() {
        String newTitle = "New Title";
        article.setTitle(newTitle);
        
        assertEquals("Title should be updated", newTitle, article.getTitle());
    }
    
    @Test
    public void setAndGetPreviewText_worksCorrectly() {
        String newPreview = "New Preview Text";
        article.setPreviewText(newPreview);
        
        assertEquals("Preview text should be updated", newPreview, article.getPreviewText());
    }
    
    @Test
    public void setAndGetThumbnailUrl_worksCorrectly() {
        String newUrl = "https://example.com/new-image.jpg";
        article.setThumbnailUrl(newUrl);
        
        assertEquals("Thumbnail URL should be updated", newUrl, article.getThumbnailUrl());
    }
    
    @Test
    public void setAndGetTemplate_updatesTemplateAndId() {
        // Create test template
        Template template = new Template();
        template.setId(2L);
        template.setName("Test Template");
        
        // Set template
        article.setTemplate(template);
        
        // Verify template is set and template ID is updated
        assertEquals("Template should be set correctly", template, article.getTemplate());
        assertEquals("TemplateId should be updated to match template", template.getId(), article.getTemplateId());
    }
    
    @Test
    public void setAndGetCreatedAt_worksCorrectly() {
        String timestamp = "2023-05-27T10:15:30";
        article.setCreatedAt(timestamp);
        
        assertEquals("CreatedAt timestamp should be updated", timestamp, article.getCreatedAt());
    }
    
    @Test
    public void setAndGetUpdatedAt_worksCorrectly() {
        String timestamp = "2023-05-27T10:15:30";
        article.setUpdatedAt(timestamp);
        
        assertEquals("UpdatedAt timestamp should be updated", timestamp, article.getUpdatedAt());
    }
    
    @Test
    public void getFormattedDate_returnsCreatedAt() {
        String timestamp = "2023-05-27T10:15:30";
        article.setCreatedAt(timestamp);
        
        // For now, getFormattedDate returns the raw date string
        assertEquals("FormattedDate should equal createdAt", timestamp, article.getFormattedDate());
    }
} 