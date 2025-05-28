package com.canvamedium.util;

import com.canvamedium.model.Template;
import com.canvamedium.model.TemplateElement;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for template-related operations.
 */
public class TemplateUtil {
    
    private static final Gson gson = new Gson();
    
    /**
     * Creates an empty template layout structure.
     *
     * @return JsonObject representing an empty layout
     */
    public static JsonObject createEmptyLayout() {
        JsonObject layout = new JsonObject();
        layout.addProperty("canvasWidth", 1080);
        layout.addProperty("canvasHeight", 1920);
        layout.addProperty("backgroundColor", "#FFFFFF");
        layout.add("elements", new JsonArray());
        return layout;
    }
    
    /**
     * Extracts the elements from a template layout.
     *
     * @param template The template to extract elements from
     * @return List of TemplateElements, or empty list if none exist
     */
    public static List<TemplateElement> extractElements(Template template) {
        if (template == null || template.getLayout() == null) {
            return Collections.emptyList();
        }
        
        try {
            JsonObject layout = template.getLayout();
            if (!layout.has("elements")) {
                return Collections.emptyList();
            }
            
            JsonArray elementsArray = layout.getAsJsonArray("elements");
            List<TemplateElement> elements = new ArrayList<>();
            
            for (int i = 0; i < elementsArray.size(); i++) {
                JsonObject elementJson = elementsArray.get(i).getAsJsonObject();
                TemplateElement element = gson.fromJson(elementJson, TemplateElement.class);
                elements.add(element);
            }
            
            // Sort by zIndex
            Collections.sort(elements, Comparator.comparingInt(TemplateElement::getZIndex));
            return elements;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * Updates the elements in a template layout.
     *
     * @param template The template to update
     * @param elements The list of elements to add to the template
     * @return Updated Template object
     */
    public static Template updateElements(Template template, List<TemplateElement> elements) {
        if (template == null) {
            return null;
        }
        
        JsonObject layout = template.getLayout() != null ? template.getLayout() : createEmptyLayout();
        JsonArray elementsArray = new JsonArray();
        
        // Sort by zIndex before updating
        Collections.sort(elements, Comparator.comparingInt(TemplateElement::getZIndex));
        
        // Convert elements to JSON
        for (TemplateElement element : elements) {
            JsonObject elementJson = gson.toJsonTree(element).getAsJsonObject();
            elementsArray.add(elementJson);
        }
        
        layout.add("elements", elementsArray);
        template.setLayout(layout);
        return template;
    }
    
    /**
     * Adds an element to a template.
     *
     * @param template The template to add the element to
     * @param element  The element to add
     * @return Updated Template object
     */
    public static Template addElement(Template template, TemplateElement element) {
        List<TemplateElement> elements = extractElements(template);
        elements.add(element);
        return updateElements(template, elements);
    }
    
    /**
     * Updates an existing element in a template.
     *
     * @param template   The template containing the element
     * @param elementId  The ID of the element to update
     * @param newElement The updated element data
     * @return Updated Template object, or null if element was not found
     */
    public static Template updateElement(Template template, String elementId, TemplateElement newElement) {
        List<TemplateElement> elements = extractElements(template);
        boolean found = false;
        
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).getId().equals(elementId)) {
                elements.set(i, newElement);
                found = true;
                break;
            }
        }
        
        return found ? updateElements(template, elements) : null;
    }
    
    /**
     * Removes an element from a template.
     *
     * @param template  The template containing the element
     * @param elementId The ID of the element to remove
     * @return Updated Template object, or null if element was not found
     */
    public static Template removeElement(Template template, String elementId) {
        List<TemplateElement> elements = extractElements(template);
        boolean found = false;
        
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).getId().equals(elementId)) {
                elements.remove(i);
                found = true;
                break;
            }
        }
        
        return found ? updateElements(template, elements) : null;
    }
    
    /**
     * Extracts canvas properties from template layout.
     *
     * @param template The template to extract properties from
     * @return Map of canvas properties, or default values if not found
     */
    public static Map<String, Object> extractCanvasProperties(Template template) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("canvasWidth", 1080);
        properties.put("canvasHeight", 1920);
        properties.put("backgroundColor", "#FFFFFF");
        
        if (template == null || template.getLayout() == null) {
            return properties;
        }
        
        try {
            JsonObject layout = template.getLayout();
            
            if (layout.has("canvasWidth")) {
                properties.put("canvasWidth", layout.get("canvasWidth").getAsInt());
            }
            
            if (layout.has("canvasHeight")) {
                properties.put("canvasHeight", layout.get("canvasHeight").getAsInt());
            }
            
            if (layout.has("backgroundColor")) {
                properties.put("backgroundColor", layout.get("backgroundColor").getAsString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return properties;
    }
    
    /**
     * Updates canvas properties in a template layout.
     *
     * @param template   The template to update
     * @param properties Map of canvas properties to set
     * @return Updated Template object
     */
    public static Template updateCanvasProperties(Template template, Map<String, Object> properties) {
        if (template == null) {
            return null;
        }
        
        JsonObject layout = template.getLayout() != null ? template.getLayout() : createEmptyLayout();
        
        if (properties.containsKey("canvasWidth")) {
            layout.addProperty("canvasWidth", (int) properties.get("canvasWidth"));
        }
        
        if (properties.containsKey("canvasHeight")) {
            layout.addProperty("canvasHeight", (int) properties.get("canvasHeight"));
        }
        
        if (properties.containsKey("backgroundColor")) {
            layout.addProperty("backgroundColor", (String) properties.get("backgroundColor"));
        }
        
        template.setLayout(layout);
        return template;
    }
} 