package com.canvamedium.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Objects;

/**
 * Model class representing a content tag.
 */
public class Tag {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;

    @SerializedName("articleCount")
    private int articleCount;

    /**
     * Default constructor.
     */
    public Tag() {
    }

    /**
     * Constructor with required fields.
     *
     * @param name The name of the tag
     * @param slug The URL-friendly slug for the tag
     */
    public Tag(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    /**
     * Creates a Tag instance from a Map.
     * This is useful when parsing API responses.
     *
     * @param map The map containing tag data
     * @return A new Tag instance
     */
    public static Tag fromMap(Map<?, ?> map) {
        Tag tag = new Tag();
        
        if (map.containsKey("id")) {
            Object idObj = map.get("id");
            if (idObj instanceof Number) {
                tag.setId(((Number) idObj).longValue());
            }
        }
        
        if (map.containsKey("name")) {
            Object nameObj = map.get("name");
            if (nameObj != null) {
                tag.setName(nameObj.toString());
            }
        }
        
        if (map.containsKey("slug")) {
            Object slugObj = map.get("slug");
            if (slugObj != null) {
                tag.setSlug(slugObj.toString());
            }
        }
        
        if (map.containsKey("articleCount")) {
            Object countObj = map.get("articleCount");
            if (countObj instanceof Number) {
                tag.setArticleCount(((Number) countObj).intValue());
            }
        }
        
        return tag;
    }

    /**
     * Gets the tag ID.
     *
     * @return The tag ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the tag ID.
     *
     * @param id The tag ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the tag name.
     *
     * @return The tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the tag name.
     *
     * @param name The tag name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the tag slug.
     *
     * @return The tag slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * Sets the tag slug.
     *
     * @param slug The tag slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * Gets the number of articles with this tag.
     *
     * @return The article count
     */
    public int getArticleCount() {
        return articleCount;
    }

    /**
     * Sets the number of articles with this tag.
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
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                '}';
    }
} 