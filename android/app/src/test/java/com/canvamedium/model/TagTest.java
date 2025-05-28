package com.canvamedium.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TagTest {

    @Test
    public void constructor_withRequiredFields_shouldSetNameAndSlug() {
        // Arrange & Act
        Tag tag = new Tag("Design", "design");
        
        // Assert
        assertEquals("Design", tag.getName());
        assertEquals("design", tag.getSlug());
    }
    
    @Test
    public void fromMap_withValidData_shouldCreateTagCorrectly() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("name", "Programming");
        map.put("slug", "programming");
        map.put("articleCount", 25);
        
        // Act
        Tag tag = Tag.fromMap(map);
        
        // Assert
        assertEquals(Long.valueOf(1L), tag.getId());
        assertEquals("Programming", tag.getName());
        assertEquals("programming", tag.getSlug());
        assertEquals(25, tag.getArticleCount());
    }
    
    @Test
    public void fromMap_withMissingData_shouldCreateTagWithDefaults() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Art");
        
        // Act
        Tag tag = Tag.fromMap(map);
        
        // Assert
        assertNotNull(tag);
        assertEquals("Art", tag.getName());
        assertEquals(null, tag.getSlug());
        assertEquals(0, tag.getArticleCount());
    }
    
    @Test
    public void equals_withSameId_shouldReturnTrue() {
        // Arrange
        Tag tag1 = new Tag("Design", "design");
        tag1.setId(1L);
        
        Tag tag2 = new Tag("Different Name", "different-slug");
        tag2.setId(1L);
        
        // Act & Assert
        assertEquals(tag1, tag2);
        assertEquals(tag1.hashCode(), tag2.hashCode());
    }
    
    @Test
    public void equals_withDifferentId_shouldReturnFalse() {
        // Arrange
        Tag tag1 = new Tag("Design", "design");
        tag1.setId(1L);
        
        Tag tag2 = new Tag("Design", "design");
        tag2.setId(2L);
        
        // Act & Assert
        assertNotEquals(tag1, tag2);
        assertNotEquals(tag1.hashCode(), tag2.hashCode());
    }
    
    @Test
    public void toString_shouldIncludeIdNameAndSlug() {
        // Arrange
        Tag tag = new Tag("Design", "design");
        tag.setId(1L);
        
        // Act
        String result = tag.toString();
        
        // Assert
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("name='Design'"));
        assertTrue(result.contains("slug='design'"));
    }
} 