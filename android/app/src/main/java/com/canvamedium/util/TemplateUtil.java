package com.canvamedium.util;

import com.canvamedium.model.Template;
import com.canvamedium.model.TemplateElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for handling template operations.
 */
public class TemplateUtil {

    // Cache for parsed templates to improve performance
    private static final Map<String, List<TemplateElement>> elementCache = new ConcurrentHashMap<>();
    private static final Gson gson = new GsonBuilder().create();
    
    /**
     * Creates an empty template layout.
     *
     * @return A JsonObject representing an empty layout
     */
    public static JsonObject createEmptyLayout() {
        JsonObject layout = new JsonObject();
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
        // Check cache first
        String templateId = template.getId() != null ? template.getId().toString() : "temp";
        if (elementCache.containsKey(templateId)) {
            return new ArrayList<>(elementCache.get(templateId));
        }

        List<TemplateElement> result = new ArrayList<>();

        // Get the template's layout
        JsonObject layout = template.getLayout();
        if (layout == null) {
            return result;
        }

        // Extract the elements array
        JsonArray elements = layout.getAsJsonArray("elements");
        if (elements == null) {
            return result;
        }

        // Convert each JsonObject to a TemplateElement
        for (JsonElement element : elements) {
            if (element.isJsonObject()) {
                JsonObject elementJson = element.getAsJsonObject();
                TemplateElement templateElement = gson.fromJson(elementJson, TemplateElement.class);
                result.add(templateElement);
            }
        }

        // Store in cache
        elementCache.put(templateId, new ArrayList<>(result));
        
        return result;
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

        // Invalidate cache for this template
        clearCacheForTemplate(template);

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
        // Get the template's layout
        JsonObject layout = template.getLayout();
        if (layout == null) {
            layout = createEmptyLayout();
        }

        // Extract the elements array or create a new one
        JsonArray elements = layout.getAsJsonArray("elements");
        if (elements == null) {
            elements = new JsonArray();
            layout.add("elements", elements);
        }

        // Convert element to JsonObject and add it to the elements array
        JsonObject elementJson = gson.toJsonTree(element).getAsJsonObject();
        elements.add(elementJson);

        // Update the template's layout
        template.setLayout(layout);

        // Invalidate cache for this template
        clearCacheForTemplate(template);

        return template;
    }
    
    /**
     * Updates an existing element in a template.
     *
     * @param template   The template containing the element
     * @param elementId  The ID of the element to update
     * @param element    The updated element
     * @return Updated Template object, or null if element was not found
     */
    public static Template updateElement(Template template, String elementId, TemplateElement element) {
        // Get the template's layout
        JsonObject layout = template.getLayout();
        if (layout == null) {
            return template;
        }

        // Extract the elements array
        JsonArray elements = layout.getAsJsonArray("elements");
        if (elements == null) {
            return template;
        }

        // Find the element with the matching ID and update it
        boolean found = false;
        for (int i = 0; i < elements.size(); i++) {
            JsonObject elementJson = elements.get(i).getAsJsonObject();
            if (elementJson.has("id") && elementJson.get("id").getAsString().equals(elementId)) {
                // Replace the element
                elements.set(i, gson.toJsonTree(element).getAsJsonObject());
                found = true;
                break;
            }
        }

        // If the element wasn't found, add it as a new element
        if (!found) {
            elements.add(gson.toJsonTree(element).getAsJsonObject());
        }

        // Update the template's layout
        template.setLayout(layout);

        // Invalidate cache for this template
        clearCacheForTemplate(template);

        return template;
    }
    
    /**
     * Removes an element from a template.
     *
     * @param template  The template containing the element
     * @param elementId The ID of the element to remove
     * @return Updated Template object, or null if element was not found
     */
    public static Template removeElement(Template template, String elementId) {
        // Get the template's layout
        JsonObject layout = template.getLayout();
        if (layout == null) {
            return template;
        }

        // Extract the elements array
        JsonArray elements = layout.getAsJsonArray("elements");
        if (elements == null) {
            return template;
        }

        // Find the element with the matching ID and remove it
        JsonArray updatedElements = new JsonArray();
        for (int i = 0; i < elements.size(); i++) {
            JsonObject elementJson = elements.get(i).getAsJsonObject();
            if (!elementJson.has("id") || !elementJson.get("id").getAsString().equals(elementId)) {
                // Keep elements that don't match the ID
                updatedElements.add(elementJson);
            }
        }

        // Update the elements array in the layout
        layout.add("elements", updatedElements);

        // Update the template's layout
        template.setLayout(layout);

        // Invalidate cache for this template
        clearCacheForTemplate(template);

        return template;
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

    /**
     * Gets an element by ID from a template.
     *
     * @param template  The template containing the element
     * @param elementId The ID of the element to find
     * @return The found element, or null if not found
     */
    public static TemplateElement getElementById(Template template, String elementId) {
        // Look through cached elements first for performance
        String templateId = template.getId() != null ? template.getId().toString() : "temp";
        if (elementCache.containsKey(templateId)) {
            List<TemplateElement> elements = elementCache.get(templateId);
            for (TemplateElement element : elements) {
                if (element.getId().equals(elementId)) {
                    return element;
                }
            }
        }

        // Fall back to non-cached lookup
        for (TemplateElement element : extractElements(template)) {
            if (element.getId().equals(elementId)) {
                return element;
            }
        }

        return null;
    }

    /**
     * Clears the cache for a specific template.
     *
     * @param template The template to clear cache for
     */
    private static void clearCacheForTemplate(Template template) {
        String templateId = template.getId() != null ? template.getId().toString() : "temp";
        elementCache.remove(templateId);
    }

    /**
     * Clears the entire element cache.
     */
    public static void clearCache() {
        elementCache.clear();
    }
} 