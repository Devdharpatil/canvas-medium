package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Objects;

/**
 * Model class representing a content category.
 */
public class Category {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("slug")
    private String slug;

    @SerializedName("icon")
    private String icon;

    @SerializedName("color")
    private String color;

    @SerializedName("featured")
    private boolean featured;

    @SerializedName("parentId")
    private Long parentId;

    @SerializedName("articleCount")
    private int articleCount;
    
    @SerializedName("icon_url")
    private String iconUrl;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;

    /**
     * Default constructor.
     */
    public Category() {
    }

    /**
     * Constructor with required fields.
     *
     * @param name The name of the category
     * @param slug The URL-friendly slug for the category
     */
    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
    
    /**
     * Creates a Category instance from a Map.
     * This is useful when parsing API responses.
     *
     * @param map The map containing category data
     * @return A new Category instance
     */
    public static Category fromMap(Map<?, ?> map) {
        Category category = new Category();
        
        // Extract and set the category properties from the map
        if (map.containsKey("id")) {
            Object idObj = map.get("id");
            if (idObj instanceof Number) {
                category.setId(((Number) idObj).longValue());
            }
        }
        
        if (map.containsKey("name")) {
            Object nameObj = map.get("name");
            if (nameObj != null) {
                category.setName(nameObj.toString());
            }
        }
        
        if (map.containsKey("description")) {
            Object descObj = map.get("description");
            if (descObj != null) {
                category.setDescription(descObj.toString());
            }
        }
        
        if (map.containsKey("slug")) {
            Object slugObj = map.get("slug");
            if (slugObj != null) {
                category.setSlug(slugObj.toString());
            }
        }
        
        if (map.containsKey("icon")) {
            Object iconObj = map.get("icon");
            if (iconObj != null) {
                category.setIcon(iconObj.toString());
            }
        }
        
        if (map.containsKey("icon_url")) {
            Object iconUrlObj = map.get("icon_url");
            if (iconUrlObj != null) {
                category.setIconUrl(iconUrlObj.toString());
            }
        }
        
        if (map.containsKey("color")) {
            Object colorObj = map.get("color");
            if (colorObj != null) {
                category.setColor(colorObj.toString());
            }
        }
        
        if (map.containsKey("featured")) {
            Object featuredObj = map.get("featured");
            if (featuredObj instanceof Boolean) {
                category.setFeatured((Boolean) featuredObj);
            }
        }
        
        if (map.containsKey("parentId")) {
            Object parentIdObj = map.get("parentId");
            if (parentIdObj instanceof Number) {
                category.setParentId(((Number) parentIdObj).longValue());
            }
        }
        
        if (map.containsKey("articleCount")) {
            Object countObj = map.get("articleCount");
            if (countObj instanceof Number) {
                category.setArticleCount(((Number) countObj).intValue());
            }
        }
        
        if (map.containsKey("created_at")) {
            Object createdAtObj = map.get("created_at");
            if (createdAtObj != null) {
                category.setCreatedAt(createdAtObj.toString());
            }
        }
        
        if (map.containsKey("updated_at")) {
            Object updatedAtObj = map.get("updated_at");
            if (updatedAtObj != null) {
                category.setUpdatedAt(updatedAtObj.toString());
            }
        }
        
        return category;
    }

    /**
     * Gets the category ID.
     *
     * @return The category ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the category ID.
     *
     * @param id The category ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the category name.
     *
     * @return The category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the category name.
     *
     * @param name The category name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the category description.
     *
     * @return The category description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the category description.
     *
     * @param description The category description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the category slug.
     *
     * @return The category slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Sets the category slug.
     *
     * @param slug The category slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Gets the category icon.
     *
     * @return The category icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the category icon.
     *
     * @param icon The category icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    /**
     * Gets the category icon URL.
     *
     * @return The category icon URL
     */
    public String getIconUrl() {
        return iconUrl;
    }
    
    /**
     * Sets the category icon URL.
     *
     * @param iconUrl The category icon URL
     */
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    /**
     * Gets the category color.
     *
     * @return The category color
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the category color.
     *
     * @param color The category color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Checks if the category is featured.
     *
     * @return True if the category is featured, false otherwise
     */
    public boolean isFeatured() {
        return featured;
    }

    /**
     * Sets the featured status of the category.
     *
     * @param featured The featured status
     */
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    /**
     * Gets the parent category ID.
     *
     * @return The parent category ID
     */
    public Long getParentId() {
        return parentId;
    }

    /**
     * Sets the parent category ID.
     *
     * @param parentId The parent category ID
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /**
     * Gets the number of articles in this category.
     *
     * @return The article count
     */
    public int getArticleCount() {
        return articleCount;
    }

    /**
     * Sets the number of articles in this category.
     *
     * @param articleCount The article count
     */
    public void setArticleCount(int articleCount) {
        this.articleCount = articleCount;
    }
    
    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp
     */
    public String getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the creation timestamp.
     *
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     *
     * @return The last update timestamp
     */
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt The last update timestamp
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }
} 