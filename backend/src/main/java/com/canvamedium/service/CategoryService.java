package com.canvamedium.service;

import com.canvamedium.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Category operations.
 */
public interface CategoryService {

    /**
     * Get all categories.
     *
     * @return List of all categories
     */
    List<Category> getAllCategories();

    /**
     * Get all categories with pagination.
     *
     * @param pageable Pagination information
     * @return A page of categories
     */
    Page<Category> getAllCategories(Pageable pageable);

    /**
     * Get category by ID.
     *
     * @param id The category ID
     * @return Optional containing the category if found
     */
    Optional<Category> getCategoryById(Long id);

    /**
     * Get category by slug.
     *
     * @param slug The category slug
     * @return Optional containing the category if found
     */
    Optional<Category> getCategoryBySlug(String slug);

    /**
     * Create a new category.
     *
     * @param category The category to create
     * @return The created category
     */
    Category createCategory(Category category);

    /**
     * Update an existing category.
     *
     * @param id       The ID of the category to update
     * @param category The updated category data
     * @return The updated category
     * @throws RuntimeException if the category with the given ID is not found
     */
    Category updateCategory(Long id, Category category);

    /**
     * Delete a category by ID.
     *
     * @param id The ID of the category to delete
     * @throws RuntimeException if the category with the given ID is not found
     */
    void deleteCategory(Long id);

    /**
     * Search for categories by name.
     *
     * @param query    The search query
     * @param pageable Pagination information
     * @return A page of categories matching the search criteria
     */
    Page<Category> searchCategories(String query, Pageable pageable);

    /**
     * Get featured categories.
     *
     * @param pageable Pagination information
     * @return A page of featured categories
     */
    Page<Category> getFeaturedCategories(Pageable pageable);

    /**
     * Get child categories for a parent category.
     *
     * @param parentId The parent category ID
     * @return List of child categories
     */
    List<Category> getChildCategories(Long parentId);

    /**
     * Get top-level categories (those without a parent).
     *
     * @return List of top-level categories
     */
    List<Category> getTopLevelCategories();

    /**
     * Get popular categories based on article count.
     *
     * @param pageable Pagination information
     * @return A page of categories ordered by popularity
     */
    Page<Category> getPopularCategories(Pageable pageable);

    /**
     * Set featured status for a category.
     *
     * @param id       The category ID
     * @param featured The featured status to set
     * @return The updated category
     * @throws RuntimeException if the category with the given ID is not found
     */
    Category setFeaturedStatus(Long id, boolean featured);

    /**
     * Check if a category name is available (not already in use).
     *
     * @param name The category name to check
     * @return true if the name is available, false otherwise
     */
    boolean isCategoryNameAvailable(String name);

    /**
     * Check if a category slug is available (not already in use).
     *
     * @param slug The category slug to check
     * @return true if the slug is available, false otherwise
     */
    boolean isCategorySlugAvailable(String slug);
} 