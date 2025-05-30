package com.canvamedium.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Model class representing a template element.
 * An element can be text, image, header, divider, or quote.
 */
public class TemplateElement {
    
    @Expose
    @SerializedName("id")
    private String id;
    
    @Expose
    @SerializedName("type")
    private String type;
    
    @Expose
    @SerializedName("x")
    private int x;
    
    @Expose
    @SerializedName("y")
    private int y;
    
    @Expose
    @SerializedName("width")
    private int width;
    
    @Expose
    @SerializedName("height")
    private int height;
    
    @Expose
    @SerializedName("zIndex")
    private int zIndex;
    
    @Expose
    @SerializedName("properties")
    private Map<String, Object> properties;
    
    // Transient properties that aren't serialized for better performance
    private transient Object cachedRenderedContent;
    private transient boolean isInitialized = false;

    /**
     * Default constructor required for JSON deserialization.
     */
    public TemplateElement() {
        this.id = UUID.randomUUID().toString();
        this.zIndex = 0;
        this.properties = new HashMap<>();
    }

    /**
     * Creates a new template element with the specified type and position.
     *
     * @param type   The element type (text, image, etc.)
     * @param x      The x coordinate
     * @param y      The y coordinate
     * @param width  The width
     * @param height The height
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
        this.isInitialized = true;
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
     * @param id The element ID
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
     * @param type The element type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the x coordinate.
     *
     * @return The x coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x coordinate.
     *
     * @param x The x coordinate
     */
    public void setX(int x) {
        this.x = x;
        invalidateCache();
    }

    /**
     * Gets the y coordinate.
     *
     * @return The y coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y coordinate.
     *
     * @param y The y coordinate
     */
    public void setY(int y) {
        this.y = y;
        invalidateCache();
    }

    /**
     * Gets the width.
     *
     * @return The width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Sets the width.
     *
     * @param width The width
     */
    public void setWidth(int width) {
        this.width = width;
        invalidateCache();
    }

    /**
     * Gets the height.
     *
     * @return The height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets the height.
     *
     * @param height The height
     */
    public void setHeight(int height) {
        this.height = height;
        invalidateCache();
    }

    /**
     * Gets the z-index.
     *
     * @return The z-index
     */
    public int getZIndex() {
        return zIndex;
    }

    /**
     * Sets the z-index.
     *
     * @param zIndex The z-index
     */
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * Gets the properties map.
     *
     * @return The properties map
     */
    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    /**
     * Sets the properties map.
     *
     * @param properties The properties map
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
        invalidateCache();
    }

    /**
     * Gets a property value.
     *
     * @param key The property key
     * @return The property value, or null if not found
     */
    public Object getProperty(String key) {
        return properties != null ? properties.get(key) : null;
    }

    /**
     * Adds a property.
     *
     * @param key   The property key
     * @param value The property value
     */
    public void addProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
        invalidateCache();
    }

    /**
     * Determines whether the element has been properly initialized.
     *
     * @return true if the element is initialized, false otherwise
     */
    public boolean isInitialized() {
        return isInitialized || (id != null && type != null);
    }
    
    /**
     * Sets the cached rendered content for this element.
     * This can be used to store a rendered view or bitmap for reuse.
     *
     * @param content The cached content
     */
    public void setCachedRenderedContent(Object content) {
        this.cachedRenderedContent = content;
    }
    
    /**
     * Gets the cached rendered content for this element.
     *
     * @return The cached content
     */
    public Object getCachedRenderedContent() {
        return cachedRenderedContent;
    }
    
    /**
     * Invalidates any cached content.
     */
    public void invalidateCache() {
        cachedRenderedContent = null;
    }

    /**
     * Creates a clone of this element with a new ID.
     *
     * @return A cloned element
     */
    @NonNull
    public TemplateElement clone() {
        TemplateElement clone = new TemplateElement();
        clone.type = this.type;
        clone.x = this.x;
        clone.y = this.y;
        clone.width = this.width;
        clone.height = this.height;
        clone.zIndex = this.zIndex;
        
        // Deep copy properties
        if (this.properties != null) {
            clone.properties = new HashMap<>(this.properties);
        }
        
        clone.isInitialized = this.isInitialized;
        return clone;
    }
} 