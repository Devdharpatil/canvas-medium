package com.canvamedium.service;

import com.canvamedium.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Tag operations.
 */
public interface TagService {

    /**
     * Get all tags.
     *
     * @return List of all tags
     */
    List<Tag> getAllTags();

    /**
     * Get all tags with pagination.
     *
     * @param pageable Pagination information
     * @return A page of tags
     */
    Page<Tag> getAllTags(Pageable pageable);

    /**
     * Get tag by ID.
     *
     * @param id The tag ID
     * @return Optional containing the tag if found
     */
    Optional<Tag> getTagById(Long id);

    /**
     * Get tag by slug.
     *
     * @param slug The tag slug
     * @return Optional containing the tag if found
     */
    Optional<Tag> getTagBySlug(String slug);

    /**
     * Create a new tag.
     *
     * @param tag The tag to create
     * @return The created tag
     */
    Tag createTag(Tag tag);

    /**
     * Update an existing tag.
     *
     * @param id  The ID of the tag to update
     * @param tag The updated tag data
     * @return The updated tag
     * @throws RuntimeException if the tag with the given ID is not found
     */
    Tag updateTag(Long id, Tag tag);

    /**
     * Delete a tag by ID.
     *
     * @param id The ID of the tag to delete
     * @throws RuntimeException if the tag with the given ID is not found
     */
    void deleteTag(Long id);

    /**
     * Search for tags by name.
     *
     * @param query    The search query
     * @param pageable Pagination information
     * @return A page of tags matching the search criteria
     */
    Page<Tag> searchTags(String query, Pageable pageable);

    /**
     * Get popular tags based on article count.
     *
     * @param pageable Pagination information
     * @return A page of tags ordered by popularity
     */
    Page<Tag> getPopularTags(Pageable pageable);

    /**
     * Get tags for a specific article.
     *
     * @param articleId The article ID
     * @return List of tags associated with the article
     */
    List<Tag> getTagsByArticleId(Long articleId);

    /**
     * Check if a tag name is available (not already in use).
     *
     * @param name The tag name to check
     * @return true if the name is available, false otherwise
     */
    boolean isTagNameAvailable(String name);

    /**
     * Check if a tag slug is available (not already in use).
     *
     * @param slug The tag slug to check
     * @return true if the slug is available, false otherwise
     */
    boolean isTagSlugAvailable(String slug);
} 