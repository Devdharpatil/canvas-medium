package com.canvamedium.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an element in a template layout.
 * This class is not persisted as an entity but is used for serialization within Template.layout
 */
public class TemplateElement implements Serializable {
    
    private String id;
    private String type;
    private int x;
    private int y;
    private int width;
    private int height;
    private int zIndex;
    private Map<String, Object> properties;
    
    /**
     * Default constructor that generates a random ID.
     */
    public TemplateElement() {
        this.id = UUID.randomUUID().toString();
        this.properties = new HashMap<>();
    }
    
    /**
     * Constructor for creating a new element with a specific type.
     *
     * @param type   The element type
     * @param x      The x position of the element
     * @param y      The y position of the element
     * @param width  The width of the element
     * @param height The height of the element
     */
    public TemplateElement(String type, int x, int y, int width, int height) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.zIndex = 0;
        this.properties = new HashMap<>();
    }
    
    /**
     * Gets the element ID.
     *
     * @return The element ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the element ID.
     *
     * @param id The element ID to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Gets the element type.
     *
     * @return The element type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Sets the element type.
     *
     * @param type The element type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Gets the x position of the element.
     *
     * @return The x position
     */
    public int getX() {
        return x;
    }
    
    /**
     * Sets the x position of the element.
     *
     * @param x The x position to set
     */
    public void setX(int x) {
        this.x = x;
    }
    
    /**
     * Gets the y position of the element.
     *
     * @return The y position
     */
    public int getY() {
        return y;
    }
    
    /**
     * Sets the y position of the element.
     *
     * @param y The y position to set
     */
    public void setY(int y) {
        this.y = y;
    }
    
    /**
     * Gets the width of the element.
     *
     * @return The width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Sets the width of the element.
     *
     * @param width The width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    /**
     * Gets the height of the element.
     *
     * @return The height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Sets the height of the element.
     *
     * @param height The height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }
    
    /**
     * Gets the z-index of the element, which controls layering.
     *
     * @return The z-index
     */
    public int getZIndex() {
        return zIndex;
    }
    
    /**
     * Sets the z-index of the element.
     *
     * @param zIndex The z-index to set
     */
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }
    
    /**
     * Gets the additional properties of the element.
     *
     * @return Map of properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    /**
     * Sets the additional properties of the element.
     *
     * @param properties Map of properties to set
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    /**
     * Adds a property to the element.
     *
     * @param key   The property key
     * @param value The property value
     */
    public void addProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }
    
    /**
     * Gets a property value.
     *
     * @param key The property key
     * @return The property value, or null if not found
     */
    public Object getProperty(String key) {
        if (this.properties == null) {
            return null;
        }
        return this.properties.get(key);
    }
} 