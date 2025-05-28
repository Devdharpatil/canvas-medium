package com.canvamedium.model;

import com.canvamedium.model.Article.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Article entity.
 */
public class ArticleTest {
    
    private Article article;
    private Template template;
    private JsonNode content;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a sample template
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode layoutJson = objectMapper.readTree("{\"elements\": []}");
        template = new Template("Test Template", layoutJson);
        template.setId(1L);
        
        // Create sample article content
        content = objectMapper.readTree("{\"blocks\": [{\"type\": \"text\", \"content\": \"Test content\"}]}");
        
        // Create a draft article for testing
        article = new Article("Test Article", content, "This is a preview", "thumbnail.jpg", template);
    }
    
    @Test
    void constructorShouldSetDefaultStatusToDraft() {
        assertEquals(Status.DRAFT, article.getStatus());
        assertNull(article.getPublishedAt());
    }
    
    @Test
    void constructorWithStatusShouldSetCorrectStatus() {
        Article publishedArticle = new Article(
                "Published Article", 
                content, 
                "This is a published article", 
                "published.jpg", 
                template, 
                Status.PUBLISHED);
        
        assertEquals(Status.PUBLISHED, publishedArticle.getStatus());
        assertNotNull(publishedArticle.getPublishedAt());
    }
    
    @Test
    void setStatusShouldUpdateStatus() {
        article.setStatus(Status.ARCHIVED);
        assertEquals(Status.ARCHIVED, article.getStatus());
    }
    
    @Test
    void setStatusToPublishedShouldSetPublishedAt() {
        assertNull(article.getPublishedAt());
        
        article.setStatus(Status.PUBLISHED);
        
        assertEquals(Status.PUBLISHED, article.getStatus());
        assertNotNull(article.getPublishedAt());
    }
    
    @Test
    void publishMethodShouldChangeStatusAndSetDate() {
        LocalDateTime before = LocalDateTime.now();
        article.publish();
        LocalDateTime after = LocalDateTime.now();
        
        assertEquals(Status.PUBLISHED, article.getStatus());
        assertNotNull(article.getPublishedAt());
        
        // Verify the publishedAt date is set to the current time
        assertTrue(
                !article.getPublishedAt().isBefore(before) && 
                !article.getPublishedAt().isAfter(after)
        );
    }
    
    @Test
    void publishMethodShouldNotChangeAlreadyPublishedArticle() {
        article.setStatus(Status.PUBLISHED);
        LocalDateTime firstPublishedAt = article.getPublishedAt();
        
        // Wait a moment to ensure time difference
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }
        
        // Try to publish again
        article.publish();
        
        // Status should still be PUBLISHED but date should not change
        assertEquals(Status.PUBLISHED, article.getStatus());
        assertEquals(firstPublishedAt, article.getPublishedAt());
    }
    
    @Test
    void archiveMethodShouldChangeStatus() {
        article.archive();
        assertEquals(Status.ARCHIVED, article.getStatus());
    }
    
    @Test
    void createDraftCopyShouldCreateNewDraftWithSameContent() {
        // First, publish the original article
        article.publish();
        
        // Create a draft copy
        Article draftCopy = article.createDraftCopy();
        
        // Verify the draft copy properties
        assertNotSame(article, draftCopy);
        assertEquals(article.getTitle() + " (Draft)", draftCopy.getTitle());
        assertEquals(article.getContent(), draftCopy.getContent());
        assertEquals(article.getPreviewText(), draftCopy.getPreviewText());
        assertEquals(article.getThumbnailUrl(), draftCopy.getThumbnailUrl());
        assertSame(article.getTemplate(), draftCopy.getTemplate());
        assertEquals(Status.DRAFT, draftCopy.getStatus());
        assertNotNull(draftCopy.getCreatedAt());
        assertNotNull(draftCopy.getUpdatedAt());
        assertNull(draftCopy.getPublishedAt());
    }
} 