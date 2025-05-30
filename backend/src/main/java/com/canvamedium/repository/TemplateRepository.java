package com.canvamedium.repository;

import com.canvamedium.model.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Template entity operations.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    
    /**
     * Find templates by name containing the given text (case-insensitive).
     *
     * @param name     The name to search for
     * @param pageable Pagination information
     * @return A page of templates matching the search criteria
     */
    Page<Template> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    /**
     * Find templates by exact name.
     *
     * @param name The exact name to search for
     * @return List of templates with the exact name
     */
    List<Template> findByName(String name);
    
    /**
     * Find all templates ordered by creation date (descending).
     *
     * @return List of templates ordered by creation date
     */
    List<Template> findAllByOrderByCreatedAtDesc();
} 