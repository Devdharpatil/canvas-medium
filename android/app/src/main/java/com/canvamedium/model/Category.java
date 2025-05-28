package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

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