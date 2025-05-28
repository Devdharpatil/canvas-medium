package com.canvamedium.controller;

import com.canvamedium.model.Article;
import com.canvamedium.model.Category;
import com.canvamedium.service.ArticleService;
import com.canvamedium.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * REST controller for category operations.
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management API")
public class CategoryController {

    private final CategoryService categoryService;
    private final ArticleService articleService;

    /**
     * Constructor with dependencies injection.
     *
     * @param categoryService The category service
     * @param articleService  The article service
     */
    @Autowired
    public CategoryController(CategoryService categoryService, ArticleService articleService) {
        this.categoryService = categoryService;
        this.articleService = articleService;
    }

    /**
     * Get all categories.
     *
     * @param page     The page number (0-based)
     * @param size     The page size
     * @param sortBy   The field to sort by
     * @param sortDir  The sort direction
     * @return Response entity containing the page of categories
     */
    @GetMapping
    @Operation(summary = "Get all categories", description = "Returns a paginated list of categories")
    public ResponseEntity<Page<Category>> getAllCategories(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Category> categories = categoryService.getAllCategories(pageable);
        
        return ResponseEntity.ok(categories);
    }

    /**
     * Get category by ID.
     *
     * @param id The category ID
     * @return Response entity containing the category
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns a category based on its ID")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get category by slug.
     *
     * @param slug The category slug
     * @return Response entity containing the category
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get category by slug", description = "Returns a category based on its slug")
    public ResponseEntity<Category> getCategoryBySlug(@PathVariable String slug) {
        return categoryService.getCategoryBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new category.
     *
     * @param category The category to create
     * @return Response entity containing the created category
     */
    @PostMapping
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Create a category", description = "Creates a new category")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        try {
            Category createdCategory = categoryService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing category.
     *
     * @param id       The ID of the category to update
     * @param category The updated category data
     * @return Response entity containing the updated category
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Update a category", description = "Updates an existing category")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        try {
            Category updatedCategory = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(updatedCategory);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a category.
     *
     * @param id The ID of the category to delete
     * @return Response entity with status
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category", description = "Deletes an existing category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Search for categories.
     *
     * @param query    The search query
     * @param page     The page number (0-based)
     * @param size     The page size
     * @return Response entity containing the page of categories
     */
    @GetMapping("/search")
    @Operation(summary = "Search categories", description = "Searches for categories by name")
    public ResponseEntity<Page<Category>> searchCategories(
            @Parameter(description = "Search query") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories = categoryService.searchCategories(query, pageable);
        
        return ResponseEntity.ok(categories);
    }

    /**
     * Get featured categories.
     *
     * @param page     The page number (0-based)
     * @param size     The page size
     * @return Response entity containing the page of featured categories
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured categories", description = "Returns a paginated list of featured categories")
    public ResponseEntity<Page<Category>> getFeaturedCategories(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories = categoryService.getFeaturedCategories(pageable);
        
        return ResponseEntity.ok(categories);
    }

    /**
     * Get child categories.
     *
     * @param parentId The parent category ID
     * @return Response entity containing the list of child categories
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "Get child categories", description = "Returns child categories for a parent category")
    public ResponseEntity<List<Category>> getChildCategories(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getChildCategories(parentId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Get top-level categories.
     *
     * @return Response entity containing the list of top-level categories
     */
    @GetMapping("/top-level")
    @Operation(summary = "Get top-level categories", description = "Returns categories without a parent")
    public ResponseEntity<List<Category>> getTopLevelCategories() {
        List<Category> categories = categoryService.getTopLevelCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get popular categories.
     *
     * @param page     The page number (0-based)
     * @param size     The page size
     * @return Response entity containing the page of popular categories
     */
    @GetMapping("/popular")
    @Operation(summary = "Get popular categories", description = "Returns categories ordered by popularity")
    public ResponseEntity<Page<Category>> getPopularCategories(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories = categoryService.getPopularCategories(pageable);
        
        return ResponseEntity.ok(categories);
    }

    /**
     * Set featured status for a category.
     *
     * @param id       The category ID
     * @param featured The featured status to set
     * @return Response entity containing the updated category
     */
    @PutMapping("/{id}/featured")
    @PreAuthorize("hasRole('EDITOR') or hasRole('ADMIN')")
    @Operation(summary = "Set featured status", description = "Updates the featured status of a category")
    public ResponseEntity<Category> setFeaturedStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean featured = request.get("featured");
        if (featured == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Category category = categoryService.setFeaturedStatus(id, featured);
            return ResponseEntity.ok(category);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if a category name is available.
     *
     * @param name The category name to check
     * @return Response entity indicating if the name is available
     */
    @GetMapping("/check-name/{name}")
    @Operation(summary = "Check name availability", description = "Checks if a category name is available")
    public ResponseEntity<Map<String, Boolean>> checkNameAvailability(@PathVariable String name) {
        boolean isAvailable = categoryService.isCategoryNameAvailable(name);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }

    /**
     * Check if a category slug is available.
     *
     * @param slug The category slug to check
     * @return Response entity indicating if the slug is available
     */
    @GetMapping("/check-slug/{slug}")
    @Operation(summary = "Check slug availability", description = "Checks if a category slug is available")
    public ResponseEntity<Map<String, Boolean>> checkSlugAvailability(@PathVariable String slug) {
        boolean isAvailable = categoryService.isCategorySlugAvailable(slug);
        return ResponseEntity.ok(Map.of("available", isAvailable));
    }
} 