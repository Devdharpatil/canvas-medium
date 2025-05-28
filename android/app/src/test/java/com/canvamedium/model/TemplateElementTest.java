package com.canvamedium.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the TemplateElement model class.
 */
public class TemplateElementTest {

    @Test
    public void testDefaultConstructor() {
        TemplateElement element = new TemplateElement();
        assertNotNull(element.getId());
        assertNotNull(element.getProperties());
        assertEquals(0, element.getProperties().size());
    }

    @Test
    public void testParameterizedConstructor() {
        TemplateElement element = new TemplateElement("TEXT", 10, 20, 300, 150);
        
        assertNotNull(element.getId());
        assertEquals("TEXT", element.getType());
        assertEquals(10, element.getX());
        assertEquals(20, element.getY());
        assertEquals(300, element.getWidth());
        assertEquals(150, element.getHeight());
        assertEquals(0, element.getZIndex());
        assertNotNull(element.getProperties());
    }

    @Test
    public void testSettersGetters() {
        TemplateElement element = new TemplateElement();
        
        String id = "test-id";
        element.setId(id);
        element.setType("HEADER");
        element.setX(15);
        element.setY(25);
        element.setWidth(400);
        element.setHeight(100);
        element.setZIndex(5);
        
        assertEquals(id, element.getId());
        assertEquals("HEADER", element.getType());
        assertEquals(15, element.getX());
        assertEquals(25, element.getY());
        assertEquals(400, element.getWidth());
        assertEquals(100, element.getHeight());
        assertEquals(5, element.getZIndex());
    }

    @Test
    public void testProperties() {
        TemplateElement element = new TemplateElement();
        
        // Test adding properties
        element.addProperty("text", "Test Text");
        element.addProperty("color", "#FF0000");
        element.addProperty("fontSize", 16);
        
        assertEquals(3, element.getProperties().size());
        assertEquals("Test Text", element.getProperty("text"));
        assertEquals("#FF0000", element.getProperty("color"));
        assertEquals(16, element.getProperty("fontSize"));
        
        // Test getting non-existent property
        assertNull(element.getProperty("nonexistent"));
    }
    
    @Test
    public void testSetProperties() {
        TemplateElement element = new TemplateElement();
        
        // Add some initial properties
        element.addProperty("text", "Initial Text");
        
        // Create new properties map and set it
        java.util.Map<String, Object> newProperties = new java.util.HashMap<>();
        newProperties.put("text", "New Text");
        newProperties.put("fontSize", 20);
        element.setProperties(newProperties);
        
        // Verify the properties were replaced, not merged
        assertEquals(2, element.getProperties().size());
        assertEquals("New Text", element.getProperty("text"));
        assertEquals(20, element.getProperty("fontSize"));
    }
} 