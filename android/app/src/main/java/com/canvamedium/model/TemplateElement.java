package com.canvamedium.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an element in a template layout.
 * This is used for serialization within Template.layout in the Android app.
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
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getX() {
        return x;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getZIndex() {
        return zIndex;
    }
    
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
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