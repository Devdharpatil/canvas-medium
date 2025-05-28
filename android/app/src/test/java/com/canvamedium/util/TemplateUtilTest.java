package com.canvamedium.util;

import com.canvamedium.model.Template;
import com.canvamedium.model.TemplateElement;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the TemplateUtil class.
 */
public class TemplateUtilTest {

    private Template template;
    private Gson gson = new Gson();

    @Before
    public void setUp() {
        JsonObject layout = TemplateUtil.createEmptyLayout();
        template = new Template("Test Template", layout);
    }

    @Test
    public void testCreateEmptyLayout() {
        JsonObject layout = TemplateUtil.createEmptyLayout();
        
        // Check that the layout has the expected properties
        assertEquals(1080, layout.get("canvasWidth").getAsInt());
        assertEquals(1920, layout.get("canvasHeight").getAsInt());
        assertEquals("#FFFFFF", layout.get("backgroundColor").getAsString());
        assertTrue(layout.has("elements"));
        assertEquals(0, layout.getAsJsonArray("elements").size());
    }

    @Test
    public void testExtractElementsFromEmptyLayout() {
        List<TemplateElement> elements = TemplateUtil.extractElements(template);
        
        // Empty template should have no elements
        assertNotNull(elements);
        assertEquals(0, elements.size());
    }

    @Test
    public void testAddElement() {
        // Create and add element
        TemplateElement element = new TemplateElement("TEXT", 10, 20, 300, 150);
        template = TemplateUtil.addElement(template, element);
        
        // Extract elements and verify
        List<TemplateElement> elements = TemplateUtil.extractElements(template);
        assertEquals(1, elements.size());
        assertEquals("TEXT", elements.get(0).getType());
        assertEquals(10, elements.get(0).getX());
    }

    @Test
    public void testUpdateElement() {
        // Add an element
        TemplateElement element = new TemplateElement("TEXT", 10, 20, 300, 150);
        template = TemplateUtil.addElement(template, element);
        
        // Update the element
        String elementId = element.getId();
        element.setX(50);
        element.setY(60);
        template = TemplateUtil.updateElement(template, elementId, element);
        
        // Extract and verify
        List<TemplateElement> elements = TemplateUtil.extractElements(template);
        assertEquals(1, elements.size());
        assertEquals(50, elements.get(0).getX());
        assertEquals(60, elements.get(0).getY());
    }

    @Test
    public void testRemoveElement() {
        // Add two elements
        TemplateElement element1 = new TemplateElement("TEXT", 10, 20, 300, 150);
        TemplateElement element2 = new TemplateElement("IMAGE", 100, 200, 400, 300);
        template = TemplateUtil.addElement(template, element1);
        template = TemplateUtil.addElement(template, element2);
        
        // Remove first element
        String elementId = element1.getId();
        template = TemplateUtil.removeElement(template, elementId);
        
        // Verify only second element remains
        List<TemplateElement> elements = TemplateUtil.extractElements(template);
        assertEquals(1, elements.size());
        assertEquals("IMAGE", elements.get(0).getType());
    }

    @Test
    public void testZIndexSorting() {
        // Create elements with different z-indices
        TemplateElement element1 = new TemplateElement("TEXT", 10, 20, 300, 150);
        element1.setZIndex(2);
        
        TemplateElement element2 = new TemplateElement("IMAGE", 100, 200, 400, 300);
        element2.setZIndex(1);
        
        TemplateElement element3 = new TemplateElement("HEADER", 50, 50, 300, 100);
        element3.setZIndex(3);
        
        // Add elements in random order
        template = TemplateUtil.addElement(template, element1);
        template = TemplateUtil.addElement(template, element3);
        template = TemplateUtil.addElement(template, element2);
        
        // Extract and verify sorting
        List<TemplateElement> elements = TemplateUtil.extractElements(template);
        assertEquals(3, elements.size());
        assertEquals("IMAGE", elements.get(0).getType());  // z-index 1
        assertEquals("TEXT", elements.get(1).getType());   // z-index 2
        assertEquals("HEADER", elements.get(2).getType()); // z-index 3
    }

    @Test
    public void testExtractCanvasProperties() {
        // Set custom canvas properties
        JsonObject layout = template.getLayout();
        layout.addProperty("canvasWidth", 800);
        layout.addProperty("canvasHeight", 600);
        layout.addProperty("backgroundColor", "#000000");
        template.setLayout(layout);
        
        // Extract and verify
        Map<String, Object> properties = TemplateUtil.extractCanvasProperties(template);
        assertEquals(3, properties.size());
        assertEquals(800, properties.get("canvasWidth"));
        assertEquals(600, properties.get("canvasHeight"));
        assertEquals("#000000", properties.get("backgroundColor"));
    }

    @Test
    public void testUpdateCanvasProperties() {
        // Create new properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("canvasWidth", 800);
        properties.put("canvasHeight", 600);
        properties.put("backgroundColor", "#000000");
        
        // Update template with properties
        template = TemplateUtil.updateCanvasProperties(template, properties);
        
        // Verify properties were updated
        JsonObject layout = template.getLayout();
        assertEquals(800, layout.get("canvasWidth").getAsInt());
        assertEquals(600, layout.get("canvasHeight").getAsInt());
        assertEquals("#000000", layout.get("backgroundColor").getAsString());
    }
} 