package com.canvamedium.repository;

import com.canvamedium.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category entity operations.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find a category by its name.
     *
     * @param name The name to search for
     * @return An Optional containing the found category, or empty if not found
     */
    Optional<Category> findByName(String name);

    /**
     * Find a category by its slug.
     *
     * @param slug The slug to search for
     * @return An Optional containing the found category, or empty if not found
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Find categories by name containing the given text (case-insensitive).
     *
     * @param name     The name to search for
     * @param pageable Pagination information
     * @return A page of categories matching the search criteria
     */
    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find categories by their parent ID.
     *
     * @param parentId The parent category ID
     * @return List of child categories
     */
    List<Category> findByParentId(Long parentId);

    /**
     * Find top-level categories (those without a parent).
     *
     * @return List of top-level categories
     */
    List<Category> findByParentIdIsNull();

    /**
     * Find featured categories.
     *
     * @param pageable Pagination information
     * @return A page of featured categories
     */
    Page<Category> findByFeaturedTrue(Pageable pageable);

    /**
     * Check if a category exists by name (case-insensitive).
     *
     * @param name The name to check
     * @return true if a category with the given name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check if a category exists by slug.
     *
     * @param slug The slug to check
     * @return true if a category with the given slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
    
    /**
     * Find categories with the most articles.
     *
     * @param pageable Pagination information
     * @return A page of categories ordered by article count
     */
    @Query("SELECT c, COUNT(a) as articleCount FROM Category c LEFT JOIN c.articles a GROUP BY c.id ORDER BY articleCount DESC")
    Page<Category> findCategoriesByPopularity(Pageable pageable);
} 