package com.canvamedium.model;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the Template model class.
 */
public class TemplateTest {

    private Template template;
    private JsonObject layout;

    @Before
    public void setUp() {
        layout = new JsonObject();
        layout.addProperty("canvasWidth", 1080);
        layout.addProperty("canvasHeight", 1920);
        template = new Template("Test Template", layout);
    }

    @Test
    public void testTemplateConstruction() {
        assertEquals("Test Template", template.getName());
        assertEquals(layout, template.getLayout());
        assertNull(template.getId());
        assertEquals(Integer.valueOf(1), template.getVersion());
    }

    @Test
    public void testTemplateConstructionWithAllParams() {
        Template fullTemplate = new Template(
                "Full Template", 
                "This is a test template", 
                layout, 
                "https://example.com/thumbnail.jpg");
        
        assertEquals("Full Template", fullTemplate.getName());
        assertEquals("This is a test template", fullTemplate.getDescription());
        assertEquals(layout, fullTemplate.getLayout());
        assertEquals("https://example.com/thumbnail.jpg", fullTemplate.getThumbnailUrl());
        assertEquals(Integer.valueOf(1), fullTemplate.getVersion());
    }

    @Test
    public void testTemplateSettersGetters() {
        template.setId(1L);
        template.setName("Updated Name");
        template.setDescription("Updated Description");
        template.setVersion(2);
        template.setThumbnailUrl("https://example.com/updated.jpg");
        
        assertEquals(Long.valueOf(1), template.getId());
        assertEquals("Updated Name", template.getName());
        assertEquals("Updated Description", template.getDescription());
        assertEquals(Integer.valueOf(2), template.getVersion());
        assertEquals("https://example.com/updated.jpg", template.getThumbnailUrl());
    }
    
    @Test
    public void testTemplateToString() {
        assertEquals("Test Template", template.toString());
    }
} 