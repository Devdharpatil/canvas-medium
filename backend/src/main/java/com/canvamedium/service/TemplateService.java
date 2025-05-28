package com.canvamedium.service;

import com.canvamedium.model.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Template operations.
 */
public interface TemplateService {
    
    /**
     * Get all templates.
     *
     * @return List of all templates
     */
    List<Template> getAllTemplates();
    
    /**
     * Get all templates with pagination.
     *
     * @param pageable Pagination information
     * @return A page of templates
     */
    Page<Template> getAllTemplates(Pageable pageable);
    
    /**
     * Get template by ID.
     *
     * @param id The template ID
     * @return Optional containing the template if found
     */
    Optional<Template> getTemplateById(Long id);
    
    /**
     * Create a new template.
     *
     * @param template The template to create
     * @return The created template
     */
    Template createTemplate(Template template);
    
    /**
     * Update an existing template.
     *
     * @param id       The ID of the template to update
     * @param template The updated template data
     * @return The updated template
     * @throws RuntimeException if the template with the given ID is not found
     */
    Template updateTemplate(Long id, Template template);
    
    /**
     * Delete a template by ID.
     *
     * @param id The ID of the template to delete
     * @throws RuntimeException if the template with the given ID is not found
     */
    void deleteTemplate(Long id);
    
    /**
     * Search templates by name.
     *
     * @param name     The name to search for
     * @param pageable Pagination information
     * @return A page of templates matching the search criteria
     */
    Page<Template> searchTemplatesByName(String name, Pageable pageable);
} 