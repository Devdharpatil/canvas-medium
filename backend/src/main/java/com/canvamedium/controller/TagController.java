package com.canvamedium.controller;

import com.canvamedium.model.Tag;
import com.canvamedium.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for tag operations.
 */
@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Tag management API")
public class TagController {

    private final TagService tagService;

    /**
     * Constructor with dependencies injection.
     *
     * @param tagService The tag service
     */
    @Autowired
    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Get all tags.
     *
     * @param page     The page number (0-based)
     * @param size     The page size
     * @param sortBy   The field to sort by
     * @param sortDir  The sort direction
     * @return Response entity containing the page of tags
     */
    @GetMapping
    @Operation(summary = "Get all tags", description = "Returns a paginated list of tags")
    public ResponseEntity<Page<Tag>> getAllTags(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Tag> tags = tagService.getAllTags(pageable);
        
        return ResponseEntity.ok(tags);
    }

    /**
     * Get tag by ID.
     *
     * @param id The tag ID
     * @return Response entity containing the tag
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID", description = "Returns a tag based on its ID")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        return tagService.getTagById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get tag by slug.
     *
     * @param slug The tag slug
     * @return Response entity containing the tag
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get tag by slug", description = "Returns a tag based on its slug")
    public ResponseEntity<Tag> getTagBySlug(@PathVariable String slug) {
        return tagService.getTagBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new tag.
     *
     * @param tag The tag to create
     * @return Response entity containing the created tag
     */
    @PostMapping
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Create a tag", description = "Creates a new tag")
    public ResponseEntity<Tag> createTag(@Valid @RequestBody Tag tag) {
        try {
            Tag createdTag = tagService.createTag(tag);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTag);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing tag.
     *
     * @param id  The ID of the tag to update
     * @param tag The updated tag data
     * @return Response entity containing the updated tag
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Update a tag", description = "Updates an existing tag")
    public ResponseEntity<Tag> updateTag(@PathVariable Long id, @Valid @RequestBody Tag tag) {
        try {
            Tag updatedTag = tagService.updateTag(id, tag);
            return ResponseEntity.ok(updatedTag);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a tag.
     *
     * @param id The ID of the tag to delete
     * @return Response entity with status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a tag", description = "Deletes an existing tag")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search for tags.
     *
     * @param query    The search query
     * @param page     The page number (0-based)
     * @param size     The page size
     * @return Response entity containing the page of tags
     */
    @GetMapping("/search")
    @Operation(summary = "Search tags", description = "Searches for tags by name")
    public ResponseEntity<Page<Tag>> searchTags(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Tag> tags = tagService.searchTags(query, pageable);
        
        return ResponseEntity.ok(tags);
    }

    /**
     * Get popular tags.
     *
     * @param page     The page number (0-based)
     * @param size     The page size
     * @return Response entity containing the page of popular tags
     */
    @GetMapping("/popular")
    @Operation(summary = "Get popular tags", description = "Returns tags ordered by popularity")
    public ResponseEntity<Page<Tag>> getPopularTags(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Tag> tags = tagService.getPopularTags(pageable);
        
        return ResponseEntity.ok(tags);
    }

    /**
     * Get tags for a specific article.
     *
     * @param articleId The article ID
     * @return Response entity containing the list of tags
     */
    @GetMapping("/article/{articleId}")
    @Operation(summary = "Get tags for article", description = "Returns tags associated with an article")
    public ResponseEntity<List<Tag>> getTagsByArticleId(@PathVariable Long articleId) {
        List<Tag> tags = tagService.getTagsByArticleId(articleId);
        return ResponseEntity.ok(tags);
    }

    /**
     * Check if a tag name is available.
     *
     * @param name The tag name to check
     * @return Response entity indicating if the name is available
     */
    @GetMapping("/check-name/{name}")
    @Operation(summary = "Check name availability", description = "Checks if a tag name is available")
    public ResponseEntity<Map<String, Boolean>> checkNameAvailability(@PathVariable String name) {
        boolean isAvailable = tagService.isTagNameAvailable(name);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    /**
     * Check if a tag slug is available.
     *
     * @param slug The tag slug to check
     * @return Response entity indicating if the slug is available
     */
    @GetMapping("/check-slug/{slug}")
    @Operation(summary = "Check slug availability", description = "Checks if a tag slug is available")
    public ResponseEntity<Map<String, Boolean>> checkSlugAvailability(@PathVariable String slug) {
        boolean isAvailable = tagService.isTagSlugAvailable(slug);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
} 