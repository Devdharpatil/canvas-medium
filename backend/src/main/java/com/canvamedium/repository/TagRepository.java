package com.canvamedium.repository;

import com.canvamedium.model.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tag entity operations.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find a tag by its name.
     *
     * @param name The name to search for
     * @return An Optional containing the found tag, or empty if not found
     */
    Optional<Tag> findByName(String name);

    /**
     * Find a tag by its slug.
     *
     * @param slug The slug to search for
     * @return An Optional containing the found tag, or empty if not found
     */
    Optional<Tag> findBySlug(String slug);

    /**
     * Find tags by name containing the given text (case-insensitive).
     *
     * @param name     The name to search for
     * @param pageable Pagination information
     * @return A page of tags matching the search criteria
     */
    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Check if a tag exists by name (case-insensitive).
     *
     * @param name The name to check
     * @return true if a tag with the given name exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check if a tag exists by slug.
     *
     * @param slug The slug to check
     * @return true if a tag with the given slug exists, false otherwise
     */
    boolean existsBySlug(String slug);
    
    /**
     * Find tags with the most articles.
     *
     * @param pageable Pagination information
     * @return A page of tags ordered by article count
     */
    @Query("SELECT t, COUNT(a) as articleCount FROM Tag t LEFT JOIN t.articles a GROUP BY t.id ORDER BY articleCount DESC")
    Page<Tag> findTagsByPopularity(Pageable pageable);
    
    /**
     * Find tags for a specific article.
     *
     * @param articleId The article ID
     * @return List of tags associated with the article
     */
    @Query("SELECT t FROM Tag t JOIN t.articles a WHERE a.id = :articleId")
    List<Tag> findByArticleId(@Param("articleId") Long articleId);
} 